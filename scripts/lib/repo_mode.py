"""Print repo mode for CI: template | child."""
from __future__ import annotations

import sys
from pathlib import Path

from build_sprint_model import is_template_repo


def main() -> int:
    root = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path.cwd()
    print("template" if is_template_repo(root) else "child")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
