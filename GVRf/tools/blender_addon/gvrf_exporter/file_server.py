#!/usr/bin/python3

from http.server import SimpleHTTPRequestHandler, HTTPServer
from socketserver import ThreadingMixIn
import socket, threading

class ThreadingServer(ThreadingMixIn, HTTPServer):
    _thread = None
    _is_running = False

    def handle_timeout(self):
        print("timeout")
        self.stop_server()

    def request_handler(self):
        while self._is_running:
            self.handle_request()

    def start_server(self):
        print('start server')
        self._is_running = True
        self._thread = threading.Thread(target = self.request_handler)
        self._thread.start()

    def stop_server(self):
        print('stop server')
        self._is_running = False
        self.server_close()

class FileServer:
    def __init__(self, port = 8000):
        self._port = port
        self._handler = SimpleHTTPRequestHandler
        self._server = ThreadingServer(("", self._port), self._handler)
        self._server.timeout = 60
        self.update_ipv4()

    def update_ipv4(self):
        ipv4 = socket.gethostbyname_ex(socket.getfqdn(''))[-1]
        self._ipv4 = ipv4[0]
        return ipv4

    def start_server(self):
        self._server.start_server()

    def stop_server(self):
        self._server.stop_server()
