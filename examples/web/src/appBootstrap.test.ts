import { beforeEach, describe, expect, it, vi } from "vitest";
import type { AppShellCallbacks } from "./AppShell";
import { checkForUpdates, handleRestartGuard } from "./about/aboutSession";
import { bootstrapApp } from "./appBootstrap";
import en from "./locales/en.json";

const messages = en as Record<string, string>;

vi.mock("./AppShell", () => ({
  createAppShell: vi.fn(),
}));

vi.mock("./about/aboutSession", () => ({
  handleRestartGuard: vi.fn(() => false),
  checkForUpdates: vi.fn(() => Promise.resolve(messages["about.update.current"])),
}));

vi.mock("./about/donations", () => ({
  loadDonations: vi.fn(() => Promise.resolve({ enabled: true, message: "thanks", links: [] })),
}));

vi.mock("./theme", () => ({
  initTheme: vi.fn(),
  subscribeThemeChange: vi.fn(),
}));

vi.mock("./i18n", () => ({
  t: vi.fn((key: string) => messages[key] ?? key),
}));

vi.mock("./about/applyUpdate", () => ({
  applyPwaUpdate: vi.fn(() => Promise.resolve(true)),
}));

import { createAppShell } from "./AppShell";
import { applyPwaUpdate } from "./about/applyUpdate";

const mockedCreateAppShell = vi.mocked(createAppShell);
const mockedCheckForUpdates = vi.mocked(checkForUpdates);
const mockedApplyPwaUpdate = vi.mocked(applyPwaUpdate);

describe("bootstrapApp", () => {
  let handlers: AppShellCallbacks | undefined;

  function requireHandlers(): AppShellCallbacks {
    if (!handlers) {
      throw new Error("App shell handlers were not captured");
    }
    return handlers;
  }

  beforeEach(() => {
    vi.clearAllMocks();
    handlers = undefined;
    mockedCreateAppShell.mockImplementation((_root, _state, h) => {
      handlers = h;
    });
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: { register: vi.fn(() => Promise.resolve()) },
    });
  });

  it("renders app shell on bootstrap", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => {
      expect(mockedCreateAppShell).toHaveBeenCalledWith(
        root,
        expect.objectContaining({
          updateStatus: messages["about.update.current"],
        }),
        expect.any(Object),
      );
    });
  });

  it("renders immediately before donations load completes", async () => {
    const donationsMod = await import("./about/donations");
    vi.mocked(donationsMod.loadDonations).mockImplementation(() => new Promise(() => {}));
    const root = document.createElement("div");
    mockedCreateAppShell.mockClear();
    bootstrapApp(root);
    expect(mockedCreateAppShell).toHaveBeenCalled();
  });

  it("re-renders when shell state changes", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    const callsBefore = mockedCreateAppShell.mock.calls.length;
    requireHandlers().onState({ showAbout: true });
    expect(mockedCreateAppShell.mock.calls.length).toBeGreaterThan(callsBefore);
  });

  it("refreshes update status when check toggle enabled", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    mockedCheckForUpdates.mockResolvedValueOnce(messages["about.update.available"]);
    requireHandlers().onUpdateCheckChange?.(true);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(
          ([, state]) => state.updateStatus === messages["about.update.available"],
        ),
      ).toBe(true),
    );
  });

  it("registers service worker on load", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    window.dispatchEvent(new Event("load"));
    await vi.waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalledWith("/sw.js");
    });
  });

  it("skips background update check when restart guard is active", async () => {
    vi.mocked(handleRestartGuard).mockReturnValueOnce(true);
    mockedCheckForUpdates.mockClear();
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    expect(mockedCheckForUpdates).not.toHaveBeenCalled();
  });

  it("ignores disabled update-check toggle", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    const callsBefore = mockedCheckForUpdates.mock.calls.length;
    requireHandlers().onUpdateCheckChange?.(false);
    expect(mockedCheckForUpdates.mock.calls.length).toBe(callsBefore);
  });

  it("re-renders about panel when background update completes while open", async () => {
    let resolveCheck: (value: string) => void = () => {};
    mockedCheckForUpdates.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveCheck = resolve;
        }),
    );
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onState({ showAbout: true });
    const callsBefore = mockedCreateAppShell.mock.calls.length;
    resolveCheck(messages["about.update.available"]);
    await vi.waitFor(() =>
      expect(mockedCreateAppShell.mock.calls.length).toBeGreaterThan(callsBefore),
    );
  });

  it("re-renders home banner when background update completes while about is closed", async () => {
    let resolveCheck: (value: string) => void = () => {};
    mockedCheckForUpdates.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveCheck = resolve;
        }),
    );
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    const callsBefore = mockedCreateAppShell.mock.calls.length;
    resolveCheck(`${messages["about.update.available"]}: 99.0.0`);
    await vi.waitFor(() =>
      expect(mockedCreateAppShell.mock.calls.length).toBeGreaterThan(callsBefore),
    );
    expect(
      mockedCreateAppShell.mock.calls.some(
        ([, state]) => state.updateStatus === `${messages["about.update.available"]}: 99.0.0`,
      ),
    ).toBe(true);
  });

  it("exposes apply update when a newer version is reported", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    mockedCheckForUpdates.mockResolvedValueOnce(`${messages["about.update.available"]}: 99.0.0`);
    requireHandlers().onUpdateCheckChange?.(true);
    await vi.waitFor(() => expect(handlers?.canApplyUpdate).toBe(true));
  });

  it("applies PWA update through service worker registration", async () => {
    const registration = { waiting: {} } as ServiceWorkerRegistration;
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(registration)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() => expect(mockedApplyPwaUpdate).toHaveBeenCalledWith(registration));
  });

  it("shows restarting status after apply succeeds", async () => {
    mockedApplyPwaUpdate.mockResolvedValueOnce(true);
    const registration = { waiting: {} } as ServiceWorkerRegistration;
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(registration)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(
          ([, state]) => state.updateStatus === messages["about.update.restarting"],
        ),
      ).toBe(true),
    );
  });

  it("no-ops apply when service worker registration is missing", async () => {
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(undefined)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() => expect(mockedApplyPwaUpdate).not.toHaveBeenCalled());
  });
});
