"""Hardware-aware local model recommendations. Never requires Ollama/GPU."""
from __future__ import annotations

from dataclasses import asdict, dataclass, field
from typing import Any

from local_model_probe import ollama_tags, probe_gpu, ram_total_gb
from local_resources import ollama_up, ram_gb_or_none


@dataclass(frozen=True)
class Recommendation:
    tier: str
    primary: str
    alternatives: list[str]
    modelfile: str
    custom_name: str
    num_ctx: int
    vram_mib: int | None
    ram_gb: int | None
    gpu_name: str | None
    ollama: str
    warnings: list[str] = field(default_factory=list)
    cline_provider: str = "Ollama"
    cline_context_window: int = 0

    def to_dict(self) -> dict[str, Any]:
        d = asdict(self)
        d["cline_context_window"] = self.num_ctx
        return d


def pick_tier(vram_mib: int | None) -> str:
    if vram_mib is None:
        return "entry"
    if vram_mib >= 32000:
        return "high"
    if vram_mib >= 20000:
        return "sweet_spot"
    if vram_mib >= 11000:
        return "budget"
    return "entry"


def recommend(
    *,
    vram_mib: int | None = None,
    gpu_name: str | None = None,
    ram_gb: int | None = None,
    force_ollama: str | None = None,
    detect_gpu: bool = True,
) -> Recommendation:
    warnings: list[str] = []
    if detect_gpu and vram_mib is None and gpu_name is None:
        vram_mib, gpu_name, warnings = probe_gpu()
    elif not detect_gpu and vram_mib is None:
        warnings.append("No GPU probe (nvidia-smi/rocm-smi/Apple); entry tier")
    if ram_gb is None:
        ram_gb = ram_total_gb() or ram_gb_or_none()
    tier = pick_tier(vram_mib)
    catalog = {
        "entry": (
            "qwen2.5-coder:7b",
            ["qwen2.5-coder:3b"],
            "templates/ollama/Modelfile.qwen2.5-coder-7b-16k",
            "qwen2.5-coder-agent-16k",
            16384,
        ),
        "budget": (
            "qwen2.5-coder:14b",
            ["qwen2.5-coder:7b"],
            "templates/ollama/Modelfile.qwen2.5-coder-14b-32k",
            "qwen2.5-coder-14b-agent-32k",
            32768,
        ),
        "sweet_spot": (
            "qwen3-coder:30b",
            ["qwen3.6:27b", "qwen2.5-coder:32b"],
            "templates/ollama/Modelfile.qwen3-coder-30b-64k",
            "qwen3-coder-agent-64k",
            65536,
        ),
        "high": (
            "qwen3-coder:30b",
            ["qwen3.6:27b", "qwen2.5-coder:32b"],
            "templates/ollama/Modelfile.qwen3-coder-30b-64k",
            "qwen3-coder-agent-64k",
            65536,
        ),
    }
    primary, alts, modelfile, custom, ctx = catalog[tier]
    if tier == "entry":
        warnings.append("Sub-16 GB: high supervision only; not cloud-competitive autonomy")
    if tier == "sweet_spot" and ram_gb is not None and ram_gb < 48:
        warnings.append("Sweet-spot VRAM but system RAM < 48 GB; watch KV/cache pressure")
    ollama = force_ollama or ("up" if ollama_up() else "down")
    if ollama == "down":
        warnings.append("Ollama not reachable on 127.0.0.1:11434 (optional)")
    else:
        tags = ollama_tags()
        if tags and primary not in tags and primary.split(":")[0] not in tags:
            warnings.append(f"Recommended tag not in ollama list yet: {primary}")
    return Recommendation(
        tier=tier,
        primary=primary,
        alternatives=alts,
        modelfile=modelfile,
        custom_name=custom,
        num_ctx=ctx,
        vram_mib=vram_mib,
        ram_gb=ram_gb,
        gpu_name=gpu_name,
        ollama=ollama,
        warnings=warnings,
        cline_context_window=ctx,
    )


def format_text(rec: Recommendation) -> str:
    lines = [
        f"Detected: {rec.gpu_name or 'no discrete GPU'}"
        + (f", {rec.vram_mib} MiB VRAM" if rec.vram_mib is not None else ""),
        f"System RAM: {rec.ram_gb} GB" if rec.ram_gb is not None else "System RAM: unknown",
        f"Ollama: {rec.ollama}",
        f"Recommendation ({rec.tier}):",
        f"  Primary: {rec.primary}",
        f"  Alternative: {', '.join(rec.alternatives)}",
        "Suggested actions:",
        f"  ollama pull {rec.primary}",
        f"  ollama create {rec.custom_name} -f {rec.modelfile}",
        f"Cline settings: Provider={rec.cline_provider}, Model={rec.custom_name}, "
        f"Context Window={rec.num_ctx}",
    ]
    lines.extend(f"WARN: {w}" for w in rec.warnings)
    return "\n".join(lines) + "\n"
