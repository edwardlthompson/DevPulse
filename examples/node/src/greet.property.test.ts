import * as fc from "fast-check";
import { describe, expect, it } from "vitest";

import { greet } from "./greet.js";

describe("greet properties (fast-check)", () => {
  it("always returns Hello, …!", () => {
    fc.assert(
      fc.property(fc.string(), (name) => {
        const result = greet(name);
        expect(result.startsWith("Hello, ")).toBe(true);
        expect(result.endsWith("!")).toBe(true);
      }),
    );
  });

  it("embeds trimmed non-empty names", () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1 }).filter((s) => s.trim().length > 0),
        (name) => {
          expect(greet(name)).toBe(`Hello, ${name.trim()}!`);
        },
      ),
    );
  });
});
