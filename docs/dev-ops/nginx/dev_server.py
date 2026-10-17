"""Serve the local x-form pages and proxy /api requests to Spring Boot."""

from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit
from urllib.request import Request, urlopen


HTML_ROOT = Path(__file__).resolve().parent / "html"
BACKEND = "http://127.0.0.1:8091"


class DevHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(HTML_ROOT), **kwargs)

    def do_GET(self):
        if urlsplit(self.path).path == "/":
            self.send_response(302)
            self.send_header("Location", "/admin/")
            self.end_headers()
            return
        if self.path.startswith("/api/"):
            self.proxy_api()
            return
        super().do_GET()

    def do_POST(self):
        if self.path.startswith("/api/"):
            self.proxy_api()
            return
        self.send_error(404, "Only /api/ accepts POST requests")

    def proxy_api(self):
        length = int(self.headers.get("Content-Length", "0"))
        body = self.rfile.read(length) if length else None
        headers = {}
        content_type = self.headers.get("Content-Type")
        if content_type:
            headers["Content-Type"] = content_type
        request = Request(
            BACKEND + self.path,
            data=body,
            headers=headers,
            method=self.command,
        )

        try:
            with urlopen(request, timeout=40) as response:
                status = response.status
                response_body = response.read()
                response_type = response.headers.get("Content-Type")
        except HTTPError as error:
            status = error.code
            response_body = error.read()
            response_type = error.headers.get("Content-Type")
        except URLError as error:
            response_body = (
                '{"code":"502","info":"后端暂不可用，请确认 Spring Boot 正在运行于 8091 端口"}'
            ).encode("utf-8")
            self.send_response(502)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(response_body)))
            self.end_headers()
            self.wfile.write(response_body)
            self.log_error("API proxy failed: %s", error.reason)
            return

        self.send_response(status)
        if response_type:
            self.send_header("Content-Type", response_type)
        self.send_header("Content-Length", str(len(response_body)))
        self.end_headers()
        self.wfile.write(response_body)


if __name__ == "__main__":
    address = ("127.0.0.1", 18080)
    print("x-form local pages: http://127.0.0.1:18080/admin/")
    print("API proxy target: http://127.0.0.1:8091")
    print("Press Ctrl+C to stop.")
    ThreadingHTTPServer(address, DevHandler).serve_forever()
