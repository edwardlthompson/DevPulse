import { assetUrl } from "../assetUrl";
import { t } from "../i18n";
import { clearRestartGuard, getRestartGuardKey, isRestartPending } from "./applyUpdate";
import type { AppUpdateConfig, CheckInterval } from "./types";
import {
  detectInstalledFormat,
  isNewerVersion,
  parseReleaseVersion,
  shouldCheck,
} from "./updateChecker";

export const APP_VERSION = __APP_VERSION__;
const INTERVAL_KEY = "gp-app-update-interval";
const LAST_CHECKED_KEY = "gp-app-update-last-checked";

export function getInterval(): CheckInterval {
  const stored = localStorage.getItem(INTERVAL_KEY) as CheckInterval | null;
  return stored ?? "off";
}

export function setIntervalPref(interval: CheckInterval): void {
  localStorage.setItem(INTERVAL_KEY, interval);
}

async function loadAppUpdateConfig(): Promise<AppUpdateConfig | null> {
  try {
    const res = await fetch(assetUrl("app-update.json"));
    if (!res.ok) return null;
    return (await res.json()) as AppUpdateConfig;
  } catch {
    return null;
  }
}

export async function checkForUpdates(): Promise<string> {
  const config = await loadAppUpdateConfig();
  if (!config) return t("about.update.current");

  const now = Date.now();
  const last = Number(localStorage.getItem(LAST_CHECKED_KEY) || "0") || null;
  const interval = getInterval();
  if (!shouldCheck(interval, last, now)) return t("about.update.current");

  const format = config.installed_artifact_format ?? detectInstalledFormat();
  if (format !== "pwa") return t("about.update.no_compatible");
  if (!config.release_repo || config.release_repo === "OWNER/REPO") {
    return t("about.update.current");
  }

  try {
    const res = await fetch(`https://api.github.com/repos/${config.release_repo}/releases/latest`);
    if (!res.ok) return t("about.update.current");
    localStorage.setItem(LAST_CHECKED_KEY, String(now));
    const body = (await res.json()) as { tag_name?: string };
    const latest = body.tag_name ? parseReleaseVersion(body.tag_name) : null;
    if (latest && isNewerVersion(APP_VERSION, latest)) {
      return `${t("about.update.available")}: ${latest}`;
    }
    return t("about.update.current");
  } catch {
    return t("about.update.current");
  }
}

export function handleRestartGuard(): boolean {
  const guardKey = getRestartGuardKey();
  if (isRestartPending(guardKey)) {
    clearRestartGuard(guardKey);
    return true;
  }
  return false;
}
