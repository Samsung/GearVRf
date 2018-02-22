#!/usr/bin/python3

from http.server import SimpleHTTPRequestHandler, HTTPServer
from socketserver import ThreadingMixIn
import socket, threading

class ThreadingServer(ThreadingMixIn, HTTPServer):
    pass

class file_server:
    def __init__(self, port = 8000):
        self._port = port
        self._handler = SimpleHTTPRequestHandler
        self._server = ThreadingServer(("", self._port), self._handler)
        self._thread = None
        self.update_ipv4()

    def handle_request(self):
        self._server.handle_request()

    def update_ipv4(self):
        ipv4 = socket.gethostbyname_ex(socket.getfqdn(""))[-1]
        self._ipv4 = ipv4[0]
        return ipv4

    def get_current_ipv4(self):
        return self._ipv4

    def get_port(self):
        return self._port

    def start_server(self):
        print('start server')
        self._thread = threading.Thread(target = self._server.serve_forever)
        self._thread.start()

    def stop_server(self):
        print('stop server')
        self._server.shutdown()
        self._server.server_close()
        self._thread.join()
