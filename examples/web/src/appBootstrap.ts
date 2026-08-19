import { type AppShellState, createAppShell } from "./AppShell";
import { checkForUpdates, handleRestartGuard } from "./about/aboutSession";
import { applyPwaUpdate } from "./about/applyUpdate";
import { loadDonations } from "./about/donations";
import { assetUrl } from "./assetUrl";
import { t } from "./i18n";
import { initTheme, subscribeThemeChange } from "./theme";

function isUpdateAvailableStatus(status: string): boolean {
  return status.startsWith(t("about.update.available"));
}

export function bootstrapApp(appRoot: HTMLDivElement): void {
  let state: AppShellState = {
    showAbout: false,
    showSettings: false,
    updateStatus: t("about.update.current"),
    donations: { enabled: false, message: "", links: [] },
  };

  async function handleApplyUpdate(): Promise<void> {
    if (!("serviceWorker" in navigator)) return;
    const registration = await navigator.serviceWorker.getRegistration();
    if (!registration) return;
    const applied = await applyPwaUpdate(registration);
    if (applied) {
      state = { ...state, updateStatus: t("about.update.restarting") };
      render();
    }
  }

  function render(): void {
    createAppShell(appRoot, state, {
      onState: (patch) => {
        state = { ...state, ...patch };
        render();
      },
      onUpdateCheckChange: (enabled) => {
        if (enabled) {
          void checkForUpdates().then((status) => {
            state = { ...state, updateStatus: status };
            render();
          });
        }
      },
      onApplyUpdate: () => {
        void handleApplyUpdate();
      },
      canApplyUpdate: isUpdateAvailableStatus(state.updateStatus),
    });
  }

  initTheme();
  subscribeThemeChange(() => render());
  render();
  void loadDonations().then((d) => {
    state = { ...state, donations: d };
    render();
  });

  if (!handleRestartGuard()) {
    void checkForUpdates().then((status) => {
      state = { ...state, updateStatus: status };
      render();
    });
  }

  window.addEventListener("online", render);
  window.addEventListener("offline", render);

  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register(assetUrl("sw.js")).catch(() => {});
    });
  }
}
