#!/usr/bin/env python3
"""Print a hardware-aware local model recommendation (opt-in; exit 0)."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from recommend_local_model import format_text, recommend  # noqa: E402


def main(argv: list[str] | None = None) -> int:
    p = argparse.ArgumentParser(description="Recommend a local Ollama coder model for this machine.")
    p.add_argument("--json", action="store_true", help="Machine-readable recommendation")
    args = p.parse_args(argv)
    try:
        rec = recommend()
    except (OSError, ValueError, UnicodeError) as exc:
        print(f"WARN: recommend failed: {exc}", file=sys.stderr)
        rec = recommend(vram_mib=None, gpu_name=None, force_ollama="down")
    if args.json:
        print(json.dumps(rec.to_dict(), indent=2))
    else:
        sys.stdout.write(format_text(rec))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
