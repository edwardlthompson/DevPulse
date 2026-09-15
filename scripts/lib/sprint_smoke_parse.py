"""Parse BUILD_PLAN sprint blocks and numbered owner rows for sprint smoke."""
from __future__ import annotations

import re
from dataclasses import dataclass, field

HEADER = re.compile(r"^###\s+(?P<title>(?:M\d+|Sprint\s+).+)$")
ROW = re.compile(
    r"^(?P<num>\d+[a-z]?)\.\s+(?P<status>🔲|✅|❌)\s+"
    r"\[(?P<owner>AGENT|AUTO|HUMAN|ADB)\]\s+(?P<task>.+)$"
)
BOARD_STOP = ("## Ongoing Maintenance", "## Archive")


@dataclass
class SmokeItem:
    number: str
    status: str
    owner: str
    task: str


@dataclass
class SmokeSprint:
    title: str
    items: list[SmokeItem] = field(default_factory=list)

    @property
    def agent_auto(self) -> list[SmokeItem]:
        return [i for i in self.items if i.owner in ("AGENT", "AUTO")]

    @property
    def checked(self) -> list[SmokeItem]:
        return [i for i in self.agent_auto if i.status == "✅"]

    @property
    def open_agent_auto(self) -> list[SmokeItem]:
        return [i for i in self.agent_auto if i.status == "🔲"]

    @property
    def complete(self) -> bool:
        return bool(self.agent_auto) and not self.open_agent_auto


def parse_sprints(text: str) -> list[SmokeSprint]:
    """Maintainer ### M* plus Child Playbook ### Sprint * blocks."""
    lines = text.splitlines()
    blocks: list[SmokeSprint] = []
    current: SmokeSprint | None = None
    for line in lines:
        if any(line.startswith(stop) for stop in BOARD_STOP):
            current = None
        if line.startswith("### "):
            match = HEADER.match(line)
            if match:
                current = SmokeSprint(title=match.group("title").strip())
                blocks.append(current)
            else:
                current = None
            continue
        if current is None:
            continue
        row = ROW.match(line)
        if row:
            current.items.append(
                SmokeItem(
                    number=row.group("num"),
                    status=row.group("status"),
                    owner=row.group("owner"),
                    task=row.group("task").strip(),
                )
            )
    return blocks


def find_sprint(sprints: list[SmokeSprint], query: str | None) -> SmokeSprint | None:
    if query:
        needle = query.strip().lower()
        for sprint in sprints:
            title = sprint.title.lower()
            if needle == title or title.startswith(needle) or needle in title:
                return sprint
        return None
    finished = [s for s in sprints if s.complete]
    if finished:
        return finished[-1]
    return next((s for s in sprints if s.open_agent_auto), None)
