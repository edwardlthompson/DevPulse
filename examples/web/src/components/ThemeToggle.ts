import { t } from "../i18n";
import { cycleThemeMode, getThemeMode, subscribeThemeChange, type ThemeMode } from "../theme";

const ICONS: Record<ThemeMode, string> = {
  system: "◎",
  light: "☀",
  dark: "☾",
};

function themeAriaLabel(mode: ThemeMode): string {
  if (mode === "system") {
    return t("theme.mode.system");
  }
  if (mode === "light") {
    return t("theme.mode.light");
  }
  return t("theme.mode.dark");
}

export function createThemeToggle(): HTMLButtonElement {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "gp-theme-toggle";
  button.setAttribute("aria-label", t("theme.toggle.label"));

  const sync = (): void => {
    const mode = getThemeMode();
    button.textContent = ICONS[mode];
    button.setAttribute("aria-label", `${t("theme.toggle.label")}: ${themeAriaLabel(mode)}`);
  };

  button.addEventListener("click", () => {
    cycleThemeMode();
    sync();
  });

  sync();
  subscribeThemeChange(sync);
  return button;
}
