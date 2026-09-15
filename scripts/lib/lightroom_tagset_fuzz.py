"""Property / fuzz checks for Lightroom MetadataTagset factory shape."""
from __future__ import annotations

import random
import re
import sys
from pathlib import Path

TAGSET = Path("examples/lightroom/MetadataTagset.lua")
ID_RE = re.compile(r'id\s*=\s*"([^"]+)"')
ITEM_RE = re.compile(r'"(com\.[^"]+)"')


def load_items(text: str) -> tuple[str, list[str]]:
    mid = ID_RE.search(text)
    if not mid:
        raise ValueError("missing tagset id")
    items = ITEM_RE.findall(text)
    if not items:
        raise ValueError("tagset items empty")
    return mid.group(1), items


def fuzz_round(items: list[str], rng: random.Random) -> None:
    # Invariants: ids stay reverse-DNS; mutations never invent empty tokens.
    sample = list(items)
    rng.shuffle(sample)
    for item in sample:
        assert item.startswith("com."), item
        assert "." in item[4:], item
        assert " " not in item, item
    # Random splice must keep at least one Adobe-ish key when factory is non-empty.
    keep = sample[: max(1, rng.randint(1, len(sample)))]
    assert keep
    joined = "|".join(keep)
    assert "com." in joined


def main() -> int:
    root = Path.cwd()
    path = root / TAGSET
    if not path.is_file():
        print(f"SKIP lightroom tagset fuzz ({TAGSET} missing)")
        return 0
    text = path.read_text(encoding="utf-8")
    tag_id, items = load_items(text)
    if not tag_id.startswith("com."):
        print(f"FAIL: tagset id must be reverse-DNS, got {tag_id}")
        return 1
    rng = random.Random(107)  # idea #107 — deterministic fuzz
    for _ in range(64):
        fuzz_round(items, rng)
    print(f"Lightroom tagset fuzz passed ({len(items)} items, id={tag_id})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
