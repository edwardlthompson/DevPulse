# Web Project Layout

> Where website source, agent documentation, and published output live in this template. Read when your stack includes web or GitHub Pages hosting.

## Folder roles

| Path | Purpose | Publish to GitHub Pages? |
|------|---------|----------------------------|
| `docs/` | Agent prompts, security playbooks, design guide, ADRs | **No** |
| [`examples/web/`](../examples/web/) | PWA/app **source** (Vite, TypeScript, tests) | **No** (source only) |
| `examples/web/dist/` | Production **build output** | **Yes** (via GitHub Actions artifact) |
| `site/` or `website/` | Optional static/markdown site (no Vite bundler) | Only with a separate workflow |
| [`design-tokens/`](../design-tokens/) | Colors, spacing, typography tokens | **No** |

**`docs/` is not your public website.** Agents are instructed to read `docs/` for project instructions. Putting HTML, marketing pages, or PWA assets in `docs/` breaks that contract and conflicts with GitHub's legacy "Publish from `/docs`" Pages source.

## Golden Path (this template)

```text
examples/web/          # edit source here
  src/
    locales/en.json    # user-visible strings (English default)
    i18n/index.ts      # t() helper
    style.css          # layout only — no copy
    design-tokens.css  # generated from design-tokens/
    theme.ts           # theme preference only
  dist/                # npm run build — do not commit; CI publishes this

.github/workflows/pages.yml   # build + deploy dist/ to GitHub Pages
docs/                         # agent documentation only — never deploy
```

Flow:

1. Push changes under `examples/web/` to `main`.
2. [`.github/workflows/pages.yml`](../.github/workflows/pages.yml) runs `npm ci`, `npm run build` with `VITE_BASE_PATH=/${{ github.event.repository.name }}/`.
3. The workflow uploads `examples/web/dist` as the Pages artifact.
4. GitHub Pages serves the built static files.

## GitHub repository settings

| Setting | Required value |
|---------|----------------|
| **Pages source** | **GitHub Actions** (not "Deploy from `/docs` branch folder") |
| **Analytics** | None in template workflow (FOSS, no tracking scripts) |

`[HUMAN]` enables Pages under **Settings → Pages** and selects **GitHub Actions** as the source. If "Deploy from `/docs`" is enabled instead, agent documentation may be exposed as a public site and the PWA deploy will conflict.

## Localization vs styles (web)

Keep user-visible copy out of stylesheets and theme code.

| Layer | Location | API |
|-------|----------|-----|
| **Strings** | `src/locales/en.json` | `t(key)` from `src/i18n/index.ts` |
| **Styles** | `style.css`, `design-tokens.css` | CSS variables `var(--gp-*)` |
| **Theme** | `theme.ts`, `ThemeToggle.ts` | Preference only; labels from `t()` |

Default locale is **English only** at bootstrap. Add `src/locales/{lang}.json` when you ship translations.

See [`docs/DESIGN_GUIDE.md`](DESIGN_GUIDE.md) for cross-stack i18n rules, shared key naming, and layout guidance for long strings and RTL.

## Optional `site/` folder

Use `site/` or `website/` only when you need a **separate** static or markdown documentation site (no Vite build). That site needs its own workflow; do not merge it into `docs/` (agent files) or `examples/web/` (PWA source).

## Pruning the web stack

If `init-project` removes the web stack:

1. Delete or disable [`.github/workflows/pages.yml`](../.github/workflows/pages.yml).
2. Remove the GitHub Pages Demo section from your `README.md` or repoint it to your new hosting path.
3. Turn off GitHub Pages in repo settings if no site remains.

## Anti-patterns

| Do not | Why |
|--------|-----|
| Put the public site in `docs/` | Collides with agent read order and START_HERE routing |
| Commit `examples/web/dist/` | Build output belongs in CI artifacts, not git history |
| Track caches or `node_modules/` | `check-repo-hygiene.sh` fails; see `docs/REPO_HYGIENE.md` |
| Put user-facing copy in CSS | Breaks localization; use `locales/*.json` |
| Hardcode strings in `main.ts` markup | Use `t()`; CI cohesion check flags literals |
| Enable "Publish from `/docs`" | Serves agent markdown as a website; wrong content |

## Related docs

- [`docs/DESIGN_GUIDE.md`](DESIGN_GUIDE.md) — tokens, themes, Android `strings.xml`, web `t()`
- [`docs/REPO_HYGIENE.md`](REPO_HYGIENE.md) — track vs ephemeral, purge, CI gates
- [`modules/web/MODULE.md`](../modules/web/MODULE.md) — PWA requirements and activation checklist
- [`examples/web/README.md`](../examples/web/README.md) — local commands and `src/` layout
