#!/usr/bin/python3

from http.server import SimpleHTTPRequestHandler, HTTPServer
import socket

class file_server:
    def __init__(self, port = 8000):
        self._port = port
        self._handler = SimpleHTTPRequestHandler
        self._server = HTTPServer(("", self._port), self._handler)
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

