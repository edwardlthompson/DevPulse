import { describe, expect, it } from "vitest";
import { greet, isOnline } from "./greet";

describe("greet", () => {
  it("returns default greeting for empty name", () => {
    expect(greet("")).toBe("Hello, world!");
    expect(greet("   ")).toBe("Hello, world!");
  });

  it("returns personalized greeting", () => {
    expect(greet("FOSS")).toBe("Hello, FOSS!");
  });
});

describe("isOnline", () => {
  it("returns a boolean", () => {
    expect(typeof isOnline()).toBe("boolean");
  });

  it("returns true when navigator is unavailable", () => {
    const original = globalThis.navigator;
    Object.defineProperty(globalThis, "navigator", { value: undefined, configurable: true });
    expect(isOnline()).toBe(true);
    Object.defineProperty(globalThis, "navigator", { value: original, configurable: true });
  });
});
