import { beforeEach, describe, expect, it } from "vitest";
import { initTheme } from "../theme";
import {
  applySettingsThemeMode,
  getSettingsThemeMode,
  isUpdateCheckEnabled,
  setUpdateCheckEnabled,
} from "./preferences";

describe("settings preferences", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("treats off interval as update check disabled", () => {
    localStorage.setItem("gp-app-update-interval", "off");
    expect(isUpdateCheckEnabled()).toBe(false);
  });

  it("enabling update check restores weekly default", () => {
    localStorage.setItem("gp-app-update-interval", "off");
    setUpdateCheckEnabled(true);
    expect(localStorage.getItem("gp-app-update-interval")).toBe("weekly");
    expect(isUpdateCheckEnabled()).toBe(true);
  });

  it("disabling update check sets off interval", () => {
    localStorage.setItem("gp-app-update-interval", "daily");
    setUpdateCheckEnabled(false);
    expect(localStorage.getItem("gp-app-update-interval")).toBe("off");
  });

  it("persists theme across initTheme reload", () => {
    applySettingsThemeMode("dark");
    initTheme();
    expect(getSettingsThemeMode()).toBe("dark");
  });
});
