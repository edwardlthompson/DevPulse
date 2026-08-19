import { describe, expect, it } from "vitest";
import {
  compareVersions,
  detectInstalledFormat,
  isNewerVersion,
  parseReleaseVersion,
  selectReleaseAsset,
  shouldCheck,
} from "./updateChecker";

describe("shouldCheck", () => {
  it("returns false when off", () => {
    expect(shouldCheck("off", null, 0)).toBe(false);
  });

  it("returns true on session when never checked", () => {
    expect(shouldCheck("on_session", null, 100)).toBe(true);
    expect(shouldCheck("on_session", 50, 100)).toBe(false);
  });

  it("respects daily weekly monthly intervals", () => {
    const day = 86_400_000;
    expect(shouldCheck("daily", 0, day)).toBe(true);
    expect(shouldCheck("daily", 0, day - 1)).toBe(false);
    expect(shouldCheck("weekly", 0, 7 * day)).toBe(true);
    expect(shouldCheck("monthly", 0, 30 * day)).toBe(true);
  });

  it("returns false for unknown interval", () => {
    expect(shouldCheck("invalid" as never, 0, 100)).toBe(false);
  });
});

describe("parseReleaseVersion", () => {
  it("parses semver tags", () => {
    expect(parseReleaseVersion("v1.2.3")).toBe("1.2.3");
    expect(parseReleaseVersion("nope")).toBeNull();
  });
});

describe("detectInstalledFormat", () => {
  it("returns pwa for web stub", () => {
    expect(detectInstalledFormat()).toBe("pwa");
  });
});

describe("isNewerVersion", () => {
  it("detects newer releases", () => {
    expect(isNewerVersion("1.0.0", "1.0.1")).toBe(true);
    expect(isNewerVersion("1.0.1", "1.0.0")).toBe(false);
    expect(isNewerVersion("1.0.0", "1.0.0")).toBe(false);
  });
});

describe("compareVersions", () => {
  it("compares semver tuples", () => {
    expect(compareVersions("1.0.0", "1.0.1")).toBeLessThan(0);
  });
});

describe("selectReleaseAsset", () => {
  it("matches exact format only", () => {
    const assets = [
      { format: "msi", url: "https://example.com/a.msi" },
      { format: "exe", url: "https://example.com/a.exe" },
    ];
    expect(selectReleaseAsset(assets, "msi")?.url).toContain(".msi");
    expect(selectReleaseAsset(assets, "exe")?.url).toContain(".exe");
    expect(selectReleaseAsset(assets, "deb")).toBeNull();
    expect(selectReleaseAsset([{ format: ".msi", url: "https://x.msi" }], "msi")?.format).toBe(
      ".msi",
    );
  });
});
