import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cycleThemeMode, getThemeMode, initTheme, isDarkTheme, setThemeMode } from "./theme";

describe("theme", () => {
  beforeEach(() => {
    document.documentElement.dataset.theme = "system";
    localStorage.clear();
    vi.stubGlobal(
      "matchMedia",
      vi.fn().mockImplementation((query: string) => ({
        matches: query.includes("dark"),
        media: query,
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("initializes from storage", () => {
    localStorage.setItem("gp-theme", "light");
    initTheme();
    expect(getThemeMode()).toBe("light");
    expect(document.documentElement.dataset.theme).toBe("light");
  });

  it("cycles system → light → dark → system", () => {
    initTheme();
    setThemeMode("system");
    expect(cycleThemeMode()).toBe("light");
    expect(cycleThemeMode()).toBe("dark");
    expect(cycleThemeMode()).toBe("system");
  });

  it("resolves dark theme for dark mode", () => {
    setThemeMode("dark");
    expect(isDarkTheme()).toBe(true);
  });
});
