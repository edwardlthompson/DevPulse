# Brand (child product rebrand)

> Playbook: [`branding/BRANDING.md`](../../branding/BRANDING.md) · Voice: [`branding/voice.md`](../../branding/voice.md)

Replace Golden Path placeholder identity with the child’s mark, colors, and pitch. **Do not** edit `branding/template/` (parent template art). Sacred: never overwrite `branding/assets/*.svg` via icon-factory.

Other IDEs: `docs/help/BRAND.md`.

## Procedure

1. Read `branding/BRANDING.md` (creative brief + logo rules + splash) and `branding/voice.md`.
2. Ask the human (or use `AGENT.md` if present) for: audience, one promise, three adjectives, do/don’t. Require `name` + `tagline` before finalizing SVGs.
3. Propose **3** mark concepts (silhouette-readable at 16px, mono-capable). Wait for HUMAN pick — do not invent a second visual system after they choose.
4. Replace files under `branding/assets/` **keeping filenames**. Update `design-tokens/design-tokens.json` colors/`meta.name`.
5. Fill `branding/product.json`: set `"mode": "product"` (child repos only), name, tagline, pitch, features.
6. Run:
   ```bash
   python3 scripts/sync-design-tokens.py
   python3 scripts/generate-project-readme.py
   ```
7. Align UI strings (`locales` / `strings.xml`), `manifest.webmanifest`, GitHub About, and splash / `theme-color` / Android splash theme to the synced mark.
8. Confirm clear space, contrast, and splash continuity rules in `BRANDING.md`.

Begin now.
