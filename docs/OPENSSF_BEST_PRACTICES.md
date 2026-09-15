# OpenSSF Best Practices badge

Public entry: [project 14564](https://www.bestpractices.dev/en/projects/14564) (formerly CII).

Live JSON (2026-09-10): **100% Passing**, `badge_level: passing`, `achieve_passing: Met`. The README hero already links the live badge. `.bestpractices.json` remains a proposal file for later Baseline/Silver edits.

Machine-written gap list (no fake Silver claims): [`OPENSSF_GAP_LIST.md`](OPENSSF_GAP_LIST.md).

Regenerate apply links: `python3 scripts/lib/bestpractices_apply.py`

## Passing is done

Do **not** start Silver/Gold unless those criteria are actually true (two-person review, signed tags, 80–90% coverage are not).

Optional next badge (not required):

1. Open the **Baseline-1** URL from the apply script and Save.
2. GitHub → Settings → Branches → `main`: require PR, block force-push and deletion, then mark **OSPS-AC-03.01 / 03.02** Met.
3. Mark **OSPS-BR-01.02** Met only after PR #98 is on `main` (branch-name check).

## What not to mark Met

- Two-person review / signed git tags / 80–90% coverage (Silver/Gold) unless they are actually true.
- Crypto rows are **N/A** (this template does not implement crypto).
- `vulnerability_report_response` is **N/A** until you receive a private report.
- `vulnerability_report_private` stays **Met** with the GitHub Advisories URL (not N/A).
