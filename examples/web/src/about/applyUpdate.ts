export const DEFAULT_RESTART_GUARD_KEY = "gp-update-restart-pending";

export function getRestartGuardKey(configKey?: string): string {
  return configKey ?? DEFAULT_RESTART_GUARD_KEY;
}

export function isRestartPending(key: string = DEFAULT_RESTART_GUARD_KEY): boolean {
  if (typeof localStorage === "undefined") return false;
  return localStorage.getItem(key) === "true";
}

export function setRestartPending(key: string = DEFAULT_RESTART_GUARD_KEY): void {
  if (typeof localStorage === "undefined") return;
  localStorage.setItem(key, "true");
}

export function clearRestartGuard(key: string = DEFAULT_RESTART_GUARD_KEY): void {
  if (typeof localStorage === "undefined") return;
  localStorage.removeItem(key);
}

let reloadScheduled = false;

export function scheduleSingleReload(key: string = DEFAULT_RESTART_GUARD_KEY): void {
  if (reloadScheduled || typeof window === "undefined") return;
  if (isRestartPending(key)) return;
  reloadScheduled = true;
  setRestartPending(key);
  window.location.reload();
}

export async function applyPwaUpdate(
  registration: ServiceWorkerRegistration,
  key: string = DEFAULT_RESTART_GUARD_KEY,
): Promise<boolean> {
  const waiting = registration.waiting;
  if (!waiting) return false;
  return new Promise((resolve) => {
    const onController = () => {
      navigator.serviceWorker.removeEventListener("controllerchange", onController);
      scheduleSingleReload(key);
      resolve(true);
    };
    navigator.serviceWorker.addEventListener("controllerchange", onController);
    waiting.postMessage({ type: "SKIP_WAITING" });
  });
}
