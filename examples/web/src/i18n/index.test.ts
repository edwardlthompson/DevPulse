import { beforeEach, describe, expect, it } from "vitest";
import { getLocale, setLocale, t } from "./index";

describe("i18n", () => {
  beforeEach(() => {
    setLocale("en");
  });

  it("returns English strings by default", () => {
    expect(t("app.greeting")).toBe("Hello, FOSS!");
    expect(getLocale()).toBe("en");
  });

  it("ignores unsupported locale and keeps English", () => {
    setLocale("es");
    expect(getLocale()).toBe("en");
    expect(t("app.greeting")).toBe("Hello, FOSS!");
  });

  it("falls back to key when missing", () => {
    expect(t("missing.key")).toBe("missing.key");
  });
});
