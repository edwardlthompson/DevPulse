import { describe, expect, it } from "vitest";
import { assetUrl } from "./assetUrl";

describe("assetUrl", () => {
  it("prefixes paths with Vite base URL", () => {
    expect(assetUrl("app-update.json")).toBe("/app-update.json");
    expect(assetUrl("/donations.json")).toBe("/donations.json");
  });
});
