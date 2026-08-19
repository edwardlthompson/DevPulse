import { readFileSync, writeFileSync, existsSync } from "node:fs";
import path from "node:path";
import { defineConfig, type Plugin } from "vite";

const pkg = JSON.parse(
  readFileSync(new URL("./package.json", import.meta.url), "utf-8"),
) as { version: string };

function stampServiceWorkerCache(version: string): Plugin {
  return {
    name: "stamp-sw-cache",
    closeBundle() {
      const distSw = path.resolve("dist/sw.js");
      if (!existsSync(distSw)) return;
      const content = readFileSync(distSw, "utf-8");
      writeFileSync(distSw, content.replaceAll("__APP_VERSION__", version));
    },
  };
}

export default defineConfig({
  define: {
    __APP_VERSION__: JSON.stringify(pkg.version),
  },
  plugins: [stampServiceWorkerCache(pkg.version)],
  base: process.env.VITE_BASE_PATH || "/",
  build: {
    rollupOptions: {
      input: {
        main: "index.html",
      },
    },
  },
});
