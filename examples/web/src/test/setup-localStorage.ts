/**
 * Node 25+ enables a broken global localStorage stub (no getItem/clear) that
 * shadows jsdom Storage. Restore an in-memory Storage for vitest.
 * See: https://github.com/vitest-dev/vitest/issues/8757
 */
function createMemoryStorage(): Storage {
  const data = new Map<string, string>();
  return {
    get length() {
      return data.size;
    },
    clear() {
      data.clear();
    },
    getItem(key: string) {
      return data.has(key) ? (data.get(key) ?? null) : null;
    },
    setItem(key: string, value: string) {
      data.set(key, String(value));
    },
    removeItem(key: string) {
      data.delete(key);
    },
    key(index: number) {
      return Array.from(data.keys())[index] ?? null;
    },
  };
}

const candidate = (globalThis as { localStorage?: Storage }).localStorage;
const broken = !candidate || typeof candidate.getItem !== "function";
if (broken) {
  const memory = createMemoryStorage();
  Object.defineProperty(globalThis, "localStorage", {
    configurable: true,
    writable: true,
    value: memory,
  });
  if (typeof window !== "undefined") {
    Object.defineProperty(window, "localStorage", {
      configurable: true,
      writable: true,
      value: memory,
    });
  }
}
