#!/usr/bin/env bash
# Track agent gate progress in .cursor/agent-progress.json (gitignored).
# Usage: agent-progress.sh status|record|next|set-feature [--json] [options]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROGRESS="$ROOT/.cursor/agent-progress.json"
mkdir -p "$ROOT/.cursor"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

$PY - "$ROOT" "$PROGRESS" "$@" << 'PY'
import json, re, sys
from datetime import datetime, timezone
from pathlib import Path

root = Path(sys.argv[1])
progress_path = Path(sys.argv[2])
args = sys.argv[3:]

def load():
    if progress_path.exists():
        return json.loads(progress_path.read_text(encoding="utf-8"))
    stack = "web"
    sel = root / ".cursor/stack-selection.json"
    if sel.exists():
        stack = json.loads(sel.read_text(encoding="utf-8")).get("stack", "web")
    return {
        "updated_at": None,
        "stack": stack,
        "current_feature": None,
        "build_plan_step": None,
        "parallel_steps_completed": [],
        "parallel_sprint_done": [],
        "last_gate": None,
        "strikes": 0,
        "autofix_attempts": 0,
        "last_autofix": None,
        "next_action": None,
        "gates_passed": [],
    }

def save(data):
    data["updated_at"] = datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    progress_path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")

def parse_record_args(a):
    out = {
        "gate": "feature-gate",
        "exit": 0,
        "step": "",
        "gates_passed": [],
        "failed_stage": "",
        "log_tail": "",
        "build_plan_step": None,
    }
    i = 0
    while i < len(a):
        if a[i] == "--gate" and i + 1 < len(a):
            out["gate"] = a[i + 1]
            i += 2
        elif a[i] == "--exit" and i + 1 < len(a):
            out["exit"] = int(a[i + 1])
            i += 2
        elif a[i] == "--step" and i + 1 < len(a):
            out["step"] = a[i + 1]
            i += 2
        elif a[i] == "--build-plan-step" and i + 1 < len(a):
            raw = a[i + 1]
            out["build_plan_step"] = int(raw) if raw.isdigit() else raw
            i += 2
        elif a[i] == "--gates-passed" and i + 1 < len(a):
            out["gates_passed"] = [s.strip() for s in a[i + 1].split(",") if s.strip()]
            i += 2
        elif a[i] == "--failed-stage" and i + 1 < len(a):
            out["failed_stage"] = a[i + 1]
            i += 2
        elif a[i] == "--log-tail" and i + 1 < len(a):
            out["log_tail"] = a[i + 1]
            i += 2
        elif a[i] == "--autofix":
            out["autofix"] = True
            i += 1
        else:
            i += 1
    return out

if not args:
    print("Usage: agent-progress.sh status|record|next|set-feature|set-step|set-parallel-sprint-done [--json]", file=sys.stderr)
    sys.exit(1)

cmd = args[0]
json_out = "--json" in args

if cmd == "status":
    data = load()
    if json_out:
        print(json.dumps(data, indent=2))
    else:
        print(f"stack={data.get('stack')} feature={data.get('current_feature')} strikes={data.get('strikes')}")
        print(f"next_action={data.get('next_action')}")
        print(f"gates_passed={data.get('gates_passed')}")
        lg = data.get("last_gate") or {}
        print(f"last_gate={lg.get('script')} exit={lg.get('exit_code')} step={lg.get('step')}")
    sys.exit(0)

if cmd == "set-feature":
    name = ""
    i = 1
    while i < len(args):
        if args[i] == "--name" and i + 1 < len(args):
            name = args[i + 1]
            i += 2
        else:
            i += 1
    data = load()
    data["current_feature"] = name or None
    save(data)
    sys.exit(0)

if cmd == "set-step":
    step_name = ""
    i = 1
    while i < len(args):
        if args[i] == "--name" and i + 1 < len(args):
            step_name = args[i + 1]
            i += 2
        else:
            i += 1
    data = load()
    completed = data.get("parallel_steps_completed") or []
    if step_name and step_name not in completed:
        completed.append(step_name)
    data["parallel_steps_completed"] = completed
    data["build_plan_step"] = step_name or data.get("build_plan_step")
    save(data)
    if json_out:
        print(json.dumps({"parallel_steps_completed": completed}, indent=2))
    sys.exit(0)

if cmd == "set-parallel-sprint-done":
    sprint_name = ""
    i = 1
    while i < len(args):
        if args[i] == "--sprint" and i + 1 < len(args):
            sprint_name = args[i + 1]
            i += 2
        else:
            i += 1
    data = load()
    done = data.get("parallel_sprint_done") or []
    if sprint_name and sprint_name not in done:
        done.append(sprint_name)
    data["parallel_sprint_done"] = done
    save(data)
    if json_out:
        print(json.dumps({"parallel_sprint_done": done}, indent=2))
    sys.exit(0)

if cmd == "record":
    rec = parse_record_args(args[1:])
    data = load()
    gate = rec["gate"]
    if gate in ("feature-gate", "feature-gate.sh"):
        if rec["exit"] == 0:
            data["strikes"] = 0
        else:
            data["strikes"] = int(data.get("strikes", 0)) + 1
    elif not rec.get("autofix"):
        pass
    if rec.get("autofix"):
        data["autofix_attempts"] = int(data.get("autofix_attempts", 0)) + 1
        data["last_autofix"] = "feature-autofix.sh"
    data["last_gate"] = {
        "script": rec["gate"],
        "exit_code": rec["exit"],
        "step": rec["step"] or None,
    }
    if rec["gates_passed"]:
        data["gates_passed"] = rec["gates_passed"]
    if rec.get("build_plan_step") is not None:
        data["build_plan_step"] = rec["build_plan_step"]
    elif rec["step"]:
        data["build_plan_step"] = rec["step"]
    if rec["exit"] != 0:
        stage = rec["failed_stage"] or rec["step"] or "gate"
        tail = (rec["log_tail"] or "")[:200]
        action = f"agent: fix {stage}"
        if tail:
            action += f" — {tail}"
        data["next_action"] = action
    else:
        data["next_action"] = "proceed to next BUILD_PLAN step"
    save(data)
    sys.exit(0)

if cmd == "next":
    lane = "child"
    i = 1
    while i < len(args):
        if args[i] == "--lane" and i + 1 < len(args):
            lane = args[i + 1]
            i += 2
        elif args[i] == "--json":
            i += 1
        else:
            i += 1
    bp = root / "BUILD_PLAN.md"
    if not bp.exists():
        print("BUILD_PLAN.md not found", file=sys.stderr)
        sys.exit(1)
    text = bp.read_text(encoding="utf-8")
    in_maintainer = False
    for line in text.splitlines():
        if line.startswith("## Template Maintainer"):
            in_maintainer = True
            continue
        if in_maintainer and line.startswith("## ") and not line.startswith("## Template Maintainer"):
            if line.startswith("## Archived"):
                break
            if lane == "maintainer":
                break
        if lane == "child" and line.startswith("## Template Maintainer"):
            break
        if lane == "maintainer" and not in_maintainer:
            continue
        open_m = r"(?:🔲|⬜|\[ \])"
        m = re.match(
            rf"^(?:\d+[a-z]?\.)\s+{open_m}\s+\[(AGENT|AUTO|HUMAN|ADB)\]\s+(.+)$",
            line,
        )
        if m:
            result = {"owner": m.group(1), "task": m.group(2).strip(), "lane": lane}
            if json_out:
                print(json.dumps(result, indent=2))
            else:
                print(f"[{result['owner']}] {result['task']}")
            sys.exit(0)
        m2 = re.match(rf"^- {open_m} \[(AGENT|AUTO|HUMAN|ADB)\]\s+(.+)$", line)
        if m2 and lane == "maintainer":
            result = {"owner": m2.group(1), "task": m2.group(2).strip(), "lane": lane}
            if json_out:
                print(json.dumps(result, indent=2))
            else:
                print(f"[{result['owner']}] {result['task']}")
            sys.exit(0)
    print("No unchecked BUILD_PLAN rows found", file=sys.stderr)
    sys.exit(1)

print(f"Unknown command: {cmd}", file=sys.stderr)
sys.exit(1)
PY
