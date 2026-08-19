import type { DonationConfig } from "../about/types";
import { t } from "../i18n";

export interface AboutPanelState {
  version: string;
  updateStatus: string;
  donations: DonationConfig;
  canApplyUpdate?: boolean;
}

export function createAboutPanel(
  state: AboutPanelState,
  onClose: () => void,
  onApplyUpdate?: () => void,
): HTMLElement {
  const panel = document.createElement("section");
  panel.className = "gp-about-panel";
  panel.setAttribute("aria-label", t("about.title"));
  panel.dataset.testid = "about-panel";

  const header = document.createElement("header");
  header.className = "gp-about-header";

  const title = document.createElement("h2");
  title.textContent = t("about.title");

  const closeBtn = document.createElement("button");
  closeBtn.type = "button";
  closeBtn.className = "gp-about-close";
  closeBtn.setAttribute("aria-label", t("about.close"));
  closeBtn.textContent = "×";
  closeBtn.addEventListener("click", onClose);

  header.append(title, closeBtn);

  const versionP = document.createElement("p");
  versionP.append(`${t("about.version")}: `);
  const versionStrong = document.createElement("strong");
  versionStrong.textContent = state.version;
  versionP.append(versionStrong);

  const formatP = document.createElement("p");
  formatP.append(`${t("about.format")}: `);
  const formatCode = document.createElement("code");
  formatCode.textContent = "pwa";
  formatP.append(formatCode);

  const statusP = document.createElement("p");
  statusP.className = "gp-about-status";
  statusP.dataset.testid = "about-status";
  statusP.setAttribute("aria-live", "polite");
  statusP.textContent = state.updateStatus;

  panel.append(header, versionP, formatP, statusP);

  if (state.canApplyUpdate && onApplyUpdate) {
    const applyBtn = document.createElement("button");
    applyBtn.type = "button";
    applyBtn.className = "gp-about-apply";
    applyBtn.dataset.testid = "about-apply";
    applyBtn.textContent = t("about.update.apply");
    applyBtn.addEventListener("click", onApplyUpdate);
    panel.append(applyBtn);
  }

  if (state.donations.enabled && state.donations.links.length > 0) {
    const donateMsg = document.createElement("p");
    donateMsg.className = "gp-about-donate-msg";
    donateMsg.textContent = state.donations.message;

    const donateList = document.createElement("ul");
    donateList.className = "gp-about-donate-links";
    for (const link of state.donations.links) {
      const item = document.createElement("li");
      const anchor = document.createElement("a");
      anchor.href = link.url;
      anchor.target = "_blank";
      anchor.rel = "noopener noreferrer";
      anchor.textContent = link.label;
      item.append(anchor);
      donateList.append(item);
    }
    panel.append(donateMsg, donateList);
  }

  return panel;
}
