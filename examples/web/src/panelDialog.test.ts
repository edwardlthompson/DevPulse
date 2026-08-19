import { afterEach, describe, expect, it, vi } from "vitest";
import { bindPanelDialog } from "./panelDialog";

describe("bindPanelDialog", () => {
  afterEach(() => {
    document.body.innerHTML = "";
  });

  function createPanel(): HTMLElement {
    const panel = document.createElement("section");
    panel.innerHTML = `
      <h2>Panel title</h2>
      <button type="button" id="first-btn">First</button>
      <button type="button" id="last-btn">Last</button>
    `;
    document.body.append(panel);
    return panel;
  }

  it("sets dialog semantics and focuses the first focusable element", () => {
    const trigger = document.createElement("button");
    trigger.id = "trigger";
    document.body.append(trigger);
    trigger.focus();

    const panel = createPanel();
    bindPanelDialog(panel, vi.fn());

    expect(panel.getAttribute("role")).toBe("dialog");
    expect(panel.getAttribute("aria-modal")).toBe("true");
    expect(panel.getAttribute("aria-labelledby")).toBeTruthy();
    expect(document.activeElement?.id).toBe("first-btn");
  });

  it("calls onClose when Escape is pressed", () => {
    const panel = createPanel();
    const onClose = vi.fn();
    bindPanelDialog(panel, onClose);

    panel.dispatchEvent(new KeyboardEvent("keydown", { key: "Escape", bubbles: true }));

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("wraps focus from last to first on Tab", () => {
    const panel = createPanel();
    bindPanelDialog(panel, vi.fn());

    const last = panel.querySelector<HTMLButtonElement>("#last-btn");
    const first = panel.querySelector<HTMLButtonElement>("#first-btn");
    last?.focus();

    panel.dispatchEvent(
      new KeyboardEvent("keydown", { key: "Tab", bubbles: true, cancelable: true }),
    );

    expect(document.activeElement).toBe(first);
  });

  it("wraps focus from first to last on Shift+Tab", () => {
    const panel = createPanel();
    bindPanelDialog(panel, vi.fn());

    const last = panel.querySelector<HTMLButtonElement>("#last-btn");
    const first = panel.querySelector<HTMLButtonElement>("#first-btn");
    first?.focus();

    panel.dispatchEvent(
      new KeyboardEvent("keydown", {
        key: "Tab",
        shiftKey: true,
        bubbles: true,
        cancelable: true,
      }),
    );

    expect(document.activeElement).toBe(last);
  });

  it("restores focus when cleanup runs", () => {
    const trigger = document.createElement("button");
    trigger.id = "trigger";
    document.body.append(trigger);
    trigger.focus();

    const panel = createPanel();
    const cleanup = bindPanelDialog(panel, vi.fn());
    cleanup();

    expect(document.activeElement).toBe(trigger);
  });
});
