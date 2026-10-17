import http.server
import socketserver
import mimetypes
import os

PORT = 8080
base_dir = os.path.dirname(__file__)

candidates = [
    os.path.join(base_dir, "build", "dist", "wasmJs", "developmentExecutable"),
    os.path.join(base_dir, "build", "dist", "wasmJs", "productionExecutable"),
    os.path.join(base_dir, "build", "dist", "js", "developmentExecutable"),
    os.path.join(base_dir, "build", "dist", "js", "productionExecutable"),
]

DIRECTORY = next((c for c in candidates if os.path.exists(c)), base_dir)

mimetypes.add_type('application/wasm', '.wasm')
mimetypes.add_type('application/javascript', '.mjs')

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

if __name__ == "__main__":
    print(f"Serving {DIRECTORY} on http://localhost:{PORT}")
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        httpd.serve_forever()
