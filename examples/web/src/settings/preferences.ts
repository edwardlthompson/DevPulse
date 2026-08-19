import { getInterval, setIntervalPref } from "../about/aboutSession";
import type { CheckInterval } from "../about/types";
import { getThemeMode, setThemeMode, type ThemeMode } from "../theme";

const DEFAULT_INTERVAL: CheckInterval = "weekly";

export function isUpdateCheckEnabled(): boolean {
  return getInterval() !== "off";
}

export function setUpdateCheckEnabled(enabled: boolean): void {
  if (!enabled) {
    setIntervalPref("off");
    return;
  }
  const current = getInterval();
  setIntervalPref(current === "off" ? DEFAULT_INTERVAL : current);
}

export function getSettingsThemeMode(): ThemeMode {
  return getThemeMode();
}

export function applySettingsThemeMode(mode: ThemeMode): void {
  setThemeMode(mode);
}
