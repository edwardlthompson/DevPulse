import "./style.css";
import { bootstrapApp } from "./appBootstrap";

const rootEl = document.querySelector<HTMLDivElement>("#app");
if (!rootEl) throw new Error("App root element not found");
bootstrapApp(rootEl);
