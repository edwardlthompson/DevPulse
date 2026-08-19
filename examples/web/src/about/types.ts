export type CheckInterval = "off" | "daily" | "weekly" | "monthly" | "on_session";

export type InstalledFormat = "pwa" | "msi" | "exe" | "deb" | "apk" | string;

export interface ReleaseAsset {
  format: string;
  url: string;
  sha256?: string;
}

export interface AppUpdateConfig {
  release_repo: string;
  manifest_url?: string | null;
  check_interval: CheckInterval;
  last_checked?: number | null;
  installed_artifact_format?: InstalledFormat | null;
  restart_guard_key?: string;
}

export interface DonationLink {
  label: string;
  url: string;
}

export interface DonationConfig {
  enabled: boolean;
  message: string;
  links: DonationLink[];
}
