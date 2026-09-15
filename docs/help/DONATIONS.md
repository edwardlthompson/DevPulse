# Donations setup walkthrough

> Configure in-app donate links and GitHub funding. Donate appears **only under Settings → App info (About)** — never in the header / TopAppBar / titlebar.

## Where donate appears

| Surface | Web | Android |
|---------|-----|---------|
| About panel | Yes — all `donations.json` links | Yes — Settings → **App info** |
| Quiet header / titlebar | **No** — do not put Venmo (or any donate) in the header | **No** — do not put Venmo (or any donate) in `TopAppBar` |
| Once-per-version launch note | Optional ethical nudge after a version change | Same |
| Update / Install dialog | Never | Never |
Contract: [`docs/features/donations-updates.md`](../features/donations-updates.md). Schema: [`schemas/golden-path/donations.schema.json`](../../schemas/golden-path/donations.schema.json).

## 1. Edit `donations.json`

1. Copy `donations.json.example` → `donations.json` at the repo root (gitignored live file).
2. Set `enabled` to `true` and list one or more `{ "label", "url" }` entries.
3. Sync into stack exemplars (or let Android Gradle copy missing assets on `preBuild`):

```bash
bash scripts/sync-exemplar-config.sh
# or after init with a URL:
python3 scripts/sync-stack-config.py . OWNER/REPO 'https://your-donate-url'

```

Android also loads `donations.json.example` at runtime if the live asset was never synced; Web `loadDonations` does the same for `/donations.json.example`.

That writes:

- `examples/web/public/donations.json`
- `examples/android/app/src/main/assets/donations.json`

Example with Venmo plus international options:

```json
{
  "enabled": true,
  "message": "If this project helps you, consider supporting development.",
  "links": [
    {
      "label": "Donate via Venmo",
      "url": "https://venmo.com/code?user_id=1857304970395648420"
    },
    {
      "label": "GitHub Sponsors",
      "url": "https://github.com/sponsors/YOUR_GITHUB_USERNAME"
    },
    {
      "label": "Liberapay",
      "url": "https://liberapay.com/YOUR_USERNAME"
    },
    {
      "label": "Open Collective",
      "url": "https://opencollective.com/YOUR_COLLECTIVE"
    },
    {
      "label": "PayPal",
      "url": "https://paypal.me/YOUR_HANDLE"
    }
  ]
}

```

Hide the block entirely with `"enabled": false` or an empty `links` array.

## 2. GitHub Sponsors (repo funding button)

GitHub’s **Sponsor** button is separate from in-app About. It uses [`.github/FUNDING.yml`](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/displaying-a-sponsor-button-in-your-repository).

### Enable Sponsors on your account

1. Open [github.com/sponsors](https://github.com/sponsors) and complete eligibility for your user or org.
2. Create a sponsorship profile (tiers optional).
3. Copy your public sponsors URL: `https://github.com/sponsors/YOUR_GITHUB_USERNAME`.

### Wire `FUNDING.yml`

Init with a usable donation URL writes a minimal file via `scripts/lib/init_extras.py` (`custom:` list). Prefer GitHub’s first-class keys when you can:

```yaml
# .github/FUNDING.yml
github: YOUR_GITHUB_USERNAME
liberapay: YOUR_USERNAME
open_collective: YOUR_COLLECTIVE
ko_fi: YOUR_USERNAME
custom:
  - https://venmo.com/code?user_id=1857304970395648420
  - https://paypal.me/YOUR_HANDLE

```

Supported keys (see GitHub docs): `github`, `patreon`, `open_collective`, `ko_fi`, `tidelift`, `community_bridge`, `liberapay`, `issuehunt`, `lfx_crowdfunding`, `polar`, `buy_me_a_coffee`, `thanks_dev`, `custom`.

Commit `.github/FUNDING.yml`, push, then confirm the **Sponsor** button on the repo home page.

Also add the same sponsors URL as a link in `donations.json` so the in-app About list matches the repo button.

## 3. International and FOSS-friendly methods

Pick what matches your bank/country. Add each as another `links[]` entry (and optionally a `FUNDING.yml` key).

| Method | Best for | Typical URL | Notes |
|--------|----------|-------------|--------|
| **GitHub Sponsors** | US + many countries via GitHub payouts | `https://github.com/sponsors/USER` | Repo Sponsor button + in-app link |
| **Liberapay** | Recurring FOSS donations, EU-friendly | `https://liberapay.com/USER` | Open-source platform; `liberapay:` in FUNDING.yml |
| **Open Collective** | Collectives / fiscal hosts | `https://opencollective.com/NAME` | Transparent budgets; `open_collective:` |
| **Ko-fi** | One-off tips | `https://ko-fi.com/USER` | `ko_fi:` in FUNDING.yml |
| **Buy Me a Coffee** | Tips / memberships | `https://buymeacoffee.com/USER` | `buy_me_a_coffee:` |
| **PayPal.me** | Broad consumer reach | `https://paypal.me/HANDLE` | Fees vary by country; use `custom:` |
| **Venmo** | US peers | Venmo code / profile URL | Default Golden Path example; US-centric |
| **Stripe Payment Link** | Cards worldwide | `https://buy.stripe.com/...` | No proprietary SDK in-app — open URL in browser only |
Rules for this template:

- External browser / Custom Tabs only — no proprietary in-app payment SDKs on the FOSS path.
- No donation tracking or dark patterns ([`docs/features/donations-updates.md`](../features/donations-updates.md)).
- Prefer HTTPS URLs that work without an app install when possible.

### International placeholders review (example file)

`donations.json.example` keeps **placeholders** (`YOUR_GITHUB_USERNAME`, `YOUR_USERNAME`, `YOUR_COLLECTIVE`, `YOUR_HANDLE`) for Sponsors / Liberapay / Open Collective / PayPal so children do not ship a maintainer’s personal links by accident. The Venmo example URL is the Golden Path US demo only — replace or remove it for non-US products. `init_extras.donation_url_usable` rejects `[INSERT …]` placeholders so FUNDING.yml is not written from blank init prompts. Gate: `tests/test_donations_placeholders.py`.

## 4. Android placement checklist (agents)

When scaffolding or fixing Android About:

1. Render donation links in `AboutScreen` under the Support development heading.
2. Expose About from **Settings** (`settings_about` → push `GpRoute.About`).
3. Do **not** add a Donate `TextButton` to `TopAppBar` actions.
4. Keep the optional once-per-version donate dialog; never mix donate onto the update Install | Later dialog.
5. Regression: `GoldenPathUiTest.donateLivesUnderSettingsAboutNotTitlebar`.

## 5. Verify

```bash
bash scripts/sync-exemplar-config.sh
python3 scripts/agent-run.py feature-gate --stack android
# or full:
python3 scripts/agent-run.py verify

```

Confirm locally: home header / titlebar has no Donate, About, or theme control; Settings → App info lists every `donations.json` link.
