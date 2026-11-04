---
name: upgrade
description: Template upgrade sim (maintainer) or child catch-up plan. Use when /upgrade or template version catch-up.
disable-model-invocation: false
---

# Template upgrade

See also: `.cursor/commands/upgrade.md` · `docs/help/UPGRADE.md` · `docs/UPGRADING_FROM_TEMPLATE.md`

Follow the command flow (sim on this template; Plan-only gap list on children; no silent `do all`).

## Linguist attribution (children)

When upgrading an existing child that lacks the Linguist block:

1. Read `templates/linguist/child.gitattributes` from upstream (or this repo if still present).
2. If `.gitattributes` is missing, copy the file to `.gitattributes`.
3. If `.gitattributes` exists, append under a `# --- linguist ---` marker (do not duplicate patterns).
4. If the child stack is `python`, comment out the `scripts/** linguist-vendored` line so product scripts still count.
5. Run `bash scripts/check-linguist-attribution.sh` and fix until exit 0.

Skip when the user passes `--no-linguist` intent or the patterns are already present.
