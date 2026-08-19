import { defineConfig } from "vitest/config";
import viteConfig from "./vite.config";

export default defineConfig({
  ...viteConfig,
  test: {
    environment: "jsdom",
    setupFiles: ["src/test/setup-localStorage.ts"],
    exclude: ["e2e/**", "node_modules/**"],
    coverage: {
      provider: "v8",
      include: [
        "src/about/updateChecker.ts",
        "src/about/aboutSession.ts",
        "src/about/donations.ts",
        "src/settings/preferences.ts",
        "src/appBootstrap.ts",
        "src/greet.ts",
      ],
      thresholds: {
        lines: 90,
        functions: 90,
        branches: 85,
        statements: 90,
      },
    },
  },
});