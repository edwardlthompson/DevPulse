# Donations setup

Donate lives **only** under Settings → About — never in the header.

1. Copy `donations.json.example` → `donations.json` (gitignored).
2. Set `enabled: true` and `{ "label", "url" }` entries.
3. Sync: `bash scripts/sync-exemplar-config.sh`

Contract + schema: [`docs/features/donations-updates.md`](../features/donations-updates.md) · [`schemas/golden-path/donations.schema.json`](../../schemas/golden-path/donations.schema.json).

Example:

```json
{
  "enabled": true,
  "message": "If this project helps you, consider supporting development.",
  "links": [{ "label": "GitHub Sponsors", "url": "https://github.com/sponsors/OWNER" }]
}

```

Optional GitHub funding: `.github/FUNDING.yml`. Full walk: feature doc above.

### International placeholders review (example file)

`donations.json.example` keeps **placeholders** (`YOUR_GITHUB_USERNAME`, `YOUR_USERNAME`, `YOUR_COLLECTIVE`, `YOUR_HANDLE`) for Sponsors / Liberapay / Open Collective / PayPal so children do not ship a maintainer’s personal links by accident.
