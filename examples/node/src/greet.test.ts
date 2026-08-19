import { describe, expect, it } from "vitest";

import { greet } from "./greet.js";

describe("greet", () => {
  it("personalizes the greeting", () => {
    expect(greet("FOSS")).toBe("Hello, FOSS!");
  });

  it("falls back to world", () => {
    expect(greet("")).toBe("Hello, world!");
    expect(greet("   ")).toBe("Hello, world!");
  });
});
