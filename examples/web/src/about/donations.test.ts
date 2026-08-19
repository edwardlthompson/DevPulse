import { describe, expect, it, vi } from "vitest";
import { loadDonations, normalizeDonations } from "./donations";

describe("normalizeDonations", () => {
  it("returns disabled config for invalid input", () => {
    expect(normalizeDonations(null)).toEqual({ enabled: false, message: "", links: [] });
  });

  it("enables when links are valid", () => {
    const result = normalizeDonations({
      enabled: true,
      message: "Support us",
      links: [{ label: "GitHub", url: "https://example.com" }],
    });
    expect(result.enabled).toBe(true);
    expect(result.links).toHaveLength(1);
  });

  it("disables when links array empty", () => {
    const result = normalizeDonations({ enabled: true, message: "x", links: [] });
    expect(result.enabled).toBe(false);
  });

  it("filters invalid links", () => {
    const result = normalizeDonations({
      enabled: true,
      message: "",
      links: [{ label: "OK", url: "https://a.test" }, { label: 1, url: "bad" } as never],
    });
    expect(result.links).toHaveLength(1);
  });

  it("loadDonations returns empty on fetch failure", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));
    await expect(loadDonations()).resolves.toEqual({ enabled: false, message: "", links: [] });
    vi.unstubAllGlobals();
  });

  it("loadDonations parses successful response", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({
          enabled: true,
          message: "hi",
          links: [{ label: "A", url: "https://a" }],
        }),
      }),
    );
    const result = await loadDonations("/donations.json");
    expect(result.enabled).toBe(true);
    vi.unstubAllGlobals();
  });

  it("loadDonations returns empty when response not ok", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false }));
    await expect(loadDonations()).resolves.toEqual({ enabled: false, message: "", links: [] });
    vi.unstubAllGlobals();
  });
});
