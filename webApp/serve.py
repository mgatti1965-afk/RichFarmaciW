import http.server
import socketserver
import mimetypes
import os

PORT = 8080
base_dir = os.path.dirname(__file__)
dev_dir = os.path.join(base_dir, "build", "dist", "wasmJs", "developmentExecutable")
prod_dir = os.path.join(base_dir, "build", "dist", "wasmJs", "productionExecutable")

DIRECTORY = dev_dir if os.path.exists(dev_dir) else prod_dir

mimetypes.add_type('application/wasm', '.wasm')
mimetypes.add_type('application/javascript', '.mjs')

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

if __name__ == "__main__":
    print(f"Serving {DIRECTORY} on http://localhost:{PORT}")
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        httpd.serve_forever()
