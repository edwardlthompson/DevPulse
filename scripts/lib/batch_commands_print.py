"""Render the novice print cheat sheet from batch-commands-print.json."""
from __future__ import annotations

import json
import sys
from pathlib import Path

from batch_commands_print_audit import audit_html

ROOT = Path(__file__).resolve().parents[2]
SCHEMA = ROOT / "schemas" / "batch-commands-print.json"
HTML_PATH = ROOT / "docs" / "help" / "batch-commands-print.html"
GROUPS = (
    ("super", "Big jobs"),
    ("start", "Getting started"),
    ("build", "Building"),
    ("check", "Docs and checks"),
    ("publish", "Publishing"),
    ("hardware", "Local hardware"),
    ("maintain", "Maintenance"),
    ("session", "Long sessions"),
    ("advanced", "Advanced (optional)"),
)
HEAD = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Agent shortcuts — print sheet</title>
<style>
body { font-family: Georgia, serif; line-height: 1.35; color: #111; }
@media screen { body { max-width: 42rem; margin: 1.5rem auto; padding: 0 1rem; } }
@media print {
  .no-print { display: none !important; }
  body { font-size: 11pt; margin: 0.6in; }
  h2.break { page-break-before: always; }
  h2.break:first-of-type { page-break-before: avoid; }
}
h1 { font-size: 1.4rem; margin-bottom: 0.3rem; }
.lead { margin-top: 0; }
.warn { border: 2px solid #111; padding: 0.5rem 0.75rem; margin: 1rem 0; }
table { width: 100%; border-collapse: collapse; margin-bottom: 1rem; }
th, td { text-align: left; vertical-align: top; padding: 0.25rem 0.4rem 0.25rem 0; border-bottom: 1px solid #ccc; }
th { font-size: 0.85rem; }
code { font-family: Consolas, "Courier New", monospace; }
footer { font-size: 0.9rem; margin-top: 1.5rem; }
</style>
</head>
<body>
<p class="no-print"><strong>How to print:</strong> open this file in a browser, then use Print (Ctrl+P or Cmd+P). Landscape is optional.</p>
<h1>Agent shortcuts</h1>
<p class="lead">Type <code>/</code> in Cursor Agent chat, then pick a name. Slash commands are Cursor-only; other tools read the matching file under <code>docs/help/</code>. First-time autonomous work: Cline in Cursor (review every diff). Do not paste API keys.</p>
<div class="warn"><strong>Publishing:</strong> <code>/push</code> and <code>/ship</code> send code to GitHub. Run them only when you intend to publish.</div>
"""
FOOT = """<footer>Other IDEs: paste <code>Read docs/help/TOUR.md and walk me through it.</code> Bookmark the on-screen list at <code>docs/help/BATCH_COMMANDS.md</code>.</footer>
</body>
</html>
"""


def load_commands(root: Path | None = None) -> dict:
    path = (root or ROOT) / "schemas" / "batch-commands-print.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    cmds = data.get("commands") if isinstance(data, dict) else None
    return cmds if isinstance(cmds, dict) else {}


def render(cmds: dict) -> str:
    parts = [HEAD]
    for group, title in GROUPS:
        rows = [(n, m) for n, m in cmds.items() if m.get("group") == group]
        if not rows:
            continue
        heading = '<h2 class="break">' if group == "start" else "<h2>"
        parts.append(
            f"{heading}{title}</h2>\n<table>\n"
            '<tr><th scope="col">Type this</th><th scope="col">When to use it</th></tr>\n'
        )
        for name, meta in rows:
            cap = str(meta.get("caption") or "").replace("<", "&lt;")
            parts.append(f"<tr><td><code>/{name}</code></td><td>{cap}</td></tr>\n")
        parts.append("</table>\n")
    parts.append(FOOT)
    return "".join(parts)


def check(root: Path, names: set[str]) -> list[str]:
    cmds = load_commands(root)
    keys = set(cmds)
    errors: list[str] = []
    if keys != names:
        missing = sorted(names - keys)
        extra = sorted(keys - names)
        if missing:
            errors.append(f"print JSON missing: {missing}")
        if extra:
            errors.append(f"print JSON extra: {extra}")
    html_path = root / "docs" / "help" / "batch-commands-print.html"
    if not html_path.is_file():
        errors.append("MISSING: docs/help/batch-commands-print.html")
        return errors
    html = html_path.read_text(encoding="utf-8")
    for name in sorted(names):
        if f"/{name}" not in html:
            errors.append(f"print HTML missing /{name}")
    expected = render(cmds)
    if html.replace("\r\n", "\n") != expected.replace("\r\n", "\n"):
        errors.append("print HTML stale; run: python3 scripts/lib/batch_commands_print.py --write")
    errors.extend(audit_html(html, cmds))
    return errors


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    if args[:1] == ["--write"]:
        HTML_PATH.write_text(render(load_commands()), encoding="utf-8")
        print(f"Wrote {HTML_PATH.as_posix()}")
        return 0
    if args[:1] == ["--check"]:
        names = set(args[1:])
        errors = check(ROOT, names)
        if errors:
            print("\n".join(errors))
            return 1
        print(f"Batch print sheet OK ({len(names)} commands)")
        return 0
    print("usage: batch_commands_print.py --write | --check NAME...")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
