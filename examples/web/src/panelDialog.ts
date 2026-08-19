const FOCUSABLE = 'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])';

export function bindPanelDialog(panel: HTMLElement, onClose: () => void): () => void {
  panel.setAttribute("role", "dialog");
  panel.setAttribute("aria-modal", "true");

  const title = panel.querySelector("h2");
  if (title && !title.id) {
    title.id = `gp-panel-title-${Math.random().toString(36).slice(2, 9)}`;
    panel.setAttribute("aria-labelledby", title.id);
  }

  const previousFocus = document.activeElement as HTMLElement | null;

  const focusable = (): HTMLElement[] =>
    [...panel.querySelectorAll<HTMLElement>(FOCUSABLE)].filter(
      (el) => !el.hasAttribute("disabled") && el.tabIndex !== -1,
    );

  const first = focusable()[0];
  first?.focus();

  const onKeyDown = (event: KeyboardEvent) => {
    if (event.key === "Escape") {
      event.preventDefault();
      onClose();
      return;
    }
    if (event.key !== "Tab") return;

    const elements = focusable();
    if (!elements.length) return;

    const firstEl = elements[0];
    const lastEl = elements[elements.length - 1];
    if (event.shiftKey && document.activeElement === firstEl) {
      event.preventDefault();
      lastEl.focus();
    } else if (!event.shiftKey && document.activeElement === lastEl) {
      event.preventDefault();
      firstEl.focus();
    }
  };

  panel.addEventListener("keydown", onKeyDown);

  return () => {
    panel.removeEventListener("keydown", onKeyDown);
    previousFocus?.focus();
  };
}
