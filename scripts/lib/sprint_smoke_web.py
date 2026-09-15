"""Web HTTP startup + document load-order probe."""
from __future__ import annotations

import time
import urllib.error
import urllib.request
from html.parser import HTMLParser
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from threading import Thread

from sprint_smoke_probes import ProbeResult


class _OrderParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.order: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        amap = {k: v for k, v in attrs}
        if tag == "link" and amap.get("href"):
            self.order.append(f"link:{amap['href']}")
        if tag == "script" and amap.get("src"):
            kind = amap.get("type") or "script"
            self.order.append(f"{kind}:{amap['src']}")


def probe_web(root: Path, budget: dict[str, int]) -> ProbeResult:
    html_path = root / "examples" / "web" / "index.html"
    if not html_path.is_file():
        return ProbeResult("web", True, "no examples/web/index.html", skipped=True)
    raw = html_path.read_text(encoding="utf-8")
    parser = _OrderParser()
    parser.feed(raw)
    directory = str(html_path.parent)

    class Handler(SimpleHTTPRequestHandler):
        def __init__(self, *args, **kwargs):
            super().__init__(*args, directory=directory, **kwargs)

        def log_message(self, *_args: object) -> None:
            return

    server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
    thread = Thread(target=server.serve_forever, daemon=True)
    thread.start()
    url = f"http://127.0.0.1:{server.server_address[1]}/"
    try:
        start = time.perf_counter()
        with urllib.request.urlopen(url, timeout=5) as resp:
            body = resp.read()
            code = resp.status
        ms = (time.perf_counter() - start) * 1000
    except (urllib.error.URLError, TimeoutError, OSError) as exc:
        return ProbeResult("web", False, f"http failed: {exc}", load_order=parser.order)
    finally:
        server.shutdown()
        server.server_close()
    if code != 200 or not body:
        return ProbeResult("web", False, f"HTTP {code}", ms, parser.order)
    if ms > budget["web_http_ms"]:
        return ProbeResult(
            "web", False, f"startup {ms:.0f}ms over budget", ms, parser.order
        )
    return ProbeResult("web", True, f"HTTP 200 in {ms:.0f}ms", ms, parser.order)
