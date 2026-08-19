import type { CheckInterval, ReleaseAsset } from "./types";

const MS_DAY = 86_400_000;

export function shouldCheck(
  interval: CheckInterval,
  lastChecked: number | null,
  now: number,
): boolean {
  if (interval === "off") return false;
  if (interval === "on_session") return lastChecked === null;
  if (lastChecked === null) return true;
  const elapsed = now - lastChecked;
  if (interval === "daily") return elapsed >= MS_DAY;
  if (interval === "weekly") return elapsed >= 7 * MS_DAY;
  if (interval === "monthly") return elapsed >= 30 * MS_DAY;
  return false;
}

export function parseReleaseVersion(tag: string): string | null {
  const match = tag.match(/v?(\d+\.\d+\.\d+)/);
  return match ? match[1] : null;
}

export function compareVersions(current: string, latest: string): number {
  const parse = (v: string) => v.split(".").map((n) => Number.parseInt(n, 10));
  const a = parse(current);
  const b = parse(latest);
  for (let i = 0; i < 3; i += 1) {
    const diff = (a[i] ?? 0) - (b[i] ?? 0);
    if (diff !== 0) return diff;
  }
  return 0;
}

export function isNewerVersion(current: string, latest: string): boolean {
  return compareVersions(current, latest) < 0;
}

export function selectReleaseAsset(
  assets: ReleaseAsset[],
  installedFormat: string,
): ReleaseAsset | null {
  const normalized = installedFormat.toLowerCase().replace(/^\./, "");
  return assets.find((a) => a.format.toLowerCase().replace(/^\./, "") === normalized) ?? null;
}

export function detectInstalledFormat(): "pwa" {
  return "pwa";
}
