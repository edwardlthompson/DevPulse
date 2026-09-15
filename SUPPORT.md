# Support

Where to ask depends on what you need. Pick the smallest channel that fits.

## Questions (how do I…?)

- Read [`docs/START_HERE.md`](docs/START_HERE.md) and the 10-minute tour: [`docs/help/TOUR.md`](docs/help/TOUR.md) (Cursor: `/tour`).
- Why a file exists: [`docs/BEST_PRACTICES.md`](docs/BEST_PRACTICES.md).
- Using Windsurf, Antigravity, or another agent: [`docs/AGENT_PORTABILITY.md`](docs/AGENT_PORTABILITY.md).
- Still stuck? Open a **GitHub Discussion** in the Q&A category (no secrets). `scripts/setup-github-repo.sh` tries to enable Discussions and create Q&A; if either is missing, a human uses Settings → General → Features → Discussions, then New category → Q&A. A question issue is the fallback.

## Bugs and crashes

A **GitHub account is required** to file (v1 has no email and no anonymous proxy). Use the **Bug Report** or **Crash Report** issue template. Do not include email, tokens, home paths, or screenshots of private data. Golden Path About → Report a bug sanitizes a preview first.

Open `crash` / `bug` issues are picked up by `/audit` (and `/maintain`) as immediate fixes (at most three per run). Issues labeled `needs-repro` wait for steps.

## Feature requests

Use the **Feature Request** (template contributors) or **Product idea** (end users) form, or a Discussions **Ideas** thread. `/ideas` ranks a short list and `/allideas` dumps the full in-scope set. Neither adds a BUILD_PLAN row until a maintainer names numbers or says `board`.

## Template improvements

Use the **Template Improvement** issue. Small, well-scoped docs or example fixes can use **Good first issue**.

## Vulnerabilities

Do **not** file a public issue. Follow [`SECURITY.md`](SECURITY.md) (private advisory).

## Maintainer notifications (`[HUMAN]`)

- Add CODEOWNERS users as repository collaborators so `feedback-notify.yml` can assign `crash`/`bug` issues (GitHub web/mobile assignment ping).
- Watch this repo → Custom → **Issues**. Settings → Notifications: Participating + Assigned. GitHub Mobile is optional. Turn off GitHub email there if you do not want inbox mail.
- The project never sends email. Weekly health check warns when the fix inbox is non-empty; it does not fail CI.

## Contributing a fix

[`CONTRIBUTING.md`](CONTRIBUTING.md) — Conventional Commits, `bash scripts/verify.sh` before the PR.
