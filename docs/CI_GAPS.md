# CI gaps (living registry)

Source of truth: [`schemas/ci-gaps.json`](../schemas/ci-gaps.json). File a [`template_improvement`](../.github/ISSUE_TEMPLATE/template_improvement.yml) issue with category **ci-gap** to propose a new row.

| Status | Meaning |
|--------|---------|
| `accepted` | Known skip or non-goal. Do not treat as a failing gate. |
| `skip` | Same as accepted, but expected to stay skipped in this environment. |
| `open` | Still a real gap; needs a BUILD_PLAN or issue follow-up. |

Current accepted skips:

- **nix-not-required-by-ci-ok** — optional Nix job must not be a `ci-ok` dependency
- **android-emulator-skip-without-sdk** — no SDK means `/emulator` skips
- **semgrep-local-skip** — local Semgrep scan skips without the binary
- **pr-ci-main-only** — GitHub CI pull_request trigger is `main` only

Do not add a live crash-proxy, Google Play Services, or a required Nix `ci-ok` need to “close” a gap.
