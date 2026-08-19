import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { checkForUpdates, getInterval, handleRestartGuard, setIntervalPref } from "./aboutSession";

describe("aboutSession interval prefs", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("defaults to off (opt-in update checks)", () => {
    expect(getInterval()).toBe("off");
  });

  it("persists interval preference", () => {
    setIntervalPref("daily");
    expect(getInterval()).toBe("daily");
  });
});

describe("checkForUpdates", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("does not persist lastChecked when GitHub fetch fails", async () => {
    setIntervalPref("daily");
    vi.stubGlobal(
      "fetch",
      vi.fn(async (url: string) => {
        if (url.endsWith("/app-update.json")) {
          return new Response(
            JSON.stringify({
              release_repo: "test-owner/test-repo",
              installed_artifact_format: "pwa",
            }),
            { status: 200 },
          );
        }
        return new Response("error", { status: 500 });
      }),
    );

    await checkForUpdates();

    expect(localStorage.getItem("gp-app-update-last-checked")).toBeNull();
  });

  it("persists lastChecked after successful GitHub fetch", async () => {
    setIntervalPref("daily");
    vi.stubGlobal(
      "fetch",
      vi.fn(async (url: string) => {
        if (url.endsWith("/app-update.json")) {
          return new Response(
            JSON.stringify({
              release_repo: "test-owner/test-repo",
              installed_artifact_format: "pwa",
            }),
            { status: 200 },
          );
        }
        return new Response(JSON.stringify({ tag_name: "v0.1.0" }), {
          status: 200,
        });
      }),
    );

    await checkForUpdates();

    expect(localStorage.getItem("gp-app-update-last-checked")).not.toBeNull();
  });

  it("reports newer version when GitHub tag is ahead", async () => {
    setIntervalPref("daily");
    vi.stubGlobal(
      "fetch",
      vi.fn(async (url: string) => {
        if (url.endsWith("/app-update.json")) {
          return new Response(
            JSON.stringify({
              release_repo: "test-owner/test-repo",
              installed_artifact_format: "pwa",
            }),
            { status: 200 },
          );
        }
        return new Response(JSON.stringify({ tag_name: "v99.0.0" }), {
          status: 200,
        });
      }),
    );

    const status = await checkForUpdates();

    expect(status).toContain("99.0.0");
  });

  it("returns current when app-update.json is missing", async () => {
    setIntervalPref("daily");
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => new Response("not found", { status: 404 })),
    );

    const status = await checkForUpdates();

    expect(status).toBe("You are on the latest version.");
  });

  it("skips GitHub fetch when interval is off", async () => {
    const fetchMock = vi.fn(async (url: string) => {
      if (url.endsWith("/app-update.json")) {
        return new Response(
          JSON.stringify({
            release_repo: "test-owner/test-repo",
            installed_artifact_format: "pwa",
          }),
          { status: 200 },
        );
      }
      return new Response("error", { status: 500 });
    });
    vi.stubGlobal("fetch", fetchMock);

    const status = await checkForUpdates();

    expect(status).toBe("You are on the latest version.");
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock.mock.calls[0]?.[0]).toContain("/app-update.json");
  });

  it("returns current when GitHub fetch throws", async () => {
    setIntervalPref("daily");
    vi.stubGlobal(
      "fetch",
      vi.fn(async (url: string) => {
        if (url.endsWith("/app-update.json")) {
          return new Response(
            JSON.stringify({
              release_repo: "test-owner/test-repo",
              installed_artifact_format: "pwa",
            }),
            { status: 200 },
          );
        }
        throw new Error("network");
      }),
    );

    const status = await checkForUpdates();

    expect(status).toBe("You are on the latest version.");
  });
});

describe("handleRestartGuard", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("clears pending restart guard and reports handled", () => {
    localStorage.setItem("gp-update-restart-pending", "true");

    expect(handleRestartGuard()).toBe(true);
    expect(localStorage.getItem("gp-update-restart-pending")).toBeNull();
  });

  it("returns false when no restart is pending", () => {
    expect(handleRestartGuard()).toBe(false);
  });
});
