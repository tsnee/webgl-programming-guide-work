import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig(({ command }) => {
  if (command === "serve") {
    return {
      server: {
        open: true,
      },
      base: "/~tsnee/webgl/",
      plugins: [scalaJSPlugin()],
    }
  } else {
    return {
      base: "https://tsnee.github.io/webgl-programming-guide-work/",
      build: {
        outDir: "_site",
      },
      plugins: [scalaJSPlugin()],
    }
  }
});
