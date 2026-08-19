import { afterEach, describe, expect, it, vi } from "vitest";
import {
  applyPwaUpdate,
  clearRestartGuard,
  getRestartGuardKey,
  isRestartPending,
  scheduleSingleReload,
  setRestartPending,
} from "./applyUpdate";

describe("restart guard", () => {
  afterEach(() => {
    clearRestartGuard("test-guard");
    vi.restoreAllMocks();
  });

  it("tracks pending restart", () => {
    setRestartPending("test-guard");
    expect(isRestartPending("test-guard")).toBe(true);
    clearRestartGuard("test-guard");
    expect(isRestartPending("test-guard")).toBe(false);
  });

  it("schedules only one reload", () => {
    const reload = vi.fn();
    vi.stubGlobal("localStorage", {
      getItem: () => null,
      setItem: vi.fn(),
      removeItem: vi.fn(),
    });
    vi.stubGlobal("window", { location: { reload } });
    scheduleSingleReload("test-guard");
    scheduleSingleReload("test-guard");
    expect(reload).toHaveBeenCalledTimes(1);
  });

  it("uses custom restart guard key", () => {
    expect(getRestartGuardKey("custom")).toBe("custom");
  });
});

describe("applyPwaUpdate", () => {
  it("returns false when no waiting worker", async () => {
    const reg = { waiting: null } as ServiceWorkerRegistration;
    await expect(applyPwaUpdate(reg)).resolves.toBe(false);
  });
});
