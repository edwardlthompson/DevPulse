import en from "../locales/en.json";

const catalogs: Record<string, Record<string, string>> = {
  en: en as Record<string, string>,
};

let currentLocale = "en";

export function getLocale(): string {
  return currentLocale;
}

export function setLocale(locale: string): void {
  if (!catalogs[locale]) {
    return;
  }
  currentLocale = locale;
  document.documentElement.lang = locale;
}

export function t(key: string): string {
  const catalog = catalogs[currentLocale] ?? catalogs.en;
  return catalog[key] ?? catalogs.en[key] ?? key;
}

export function supportedLocales(): string[] {
  return Object.keys(catalogs);
}
