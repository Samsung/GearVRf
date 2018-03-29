# Copyright 2015 Samsung Electronics Co., LTD
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from http.server import SimpleHTTPRequestHandler, HTTPServer
from socketserver import ThreadingMixIn
from threading import Thread
import socket
import platform


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
        self._thread = Thread(target=self.request_handler)
        self._thread.start()

    def stop_server(self):
        print('stop server')
        self._is_running = False
        self.server_close()


class FileServer:
    _server = None

    def __init__(self, port=8000):
        self._port = port
        self._handler = SimpleHTTPRequestHandler
        self.update_ipv4()
        self.update_url()

    def update_ipv4(self):
        # FIXME: find a better way to get ip address cross-platfrom
        if platform.system() == 'Linux':
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            try:
                s.connect(('10.255.255.255', 1))
                self._ipv4 = s.getsockname()[0]
            except:
                self._ipv4 = '127.0.0.1'
            finally:
                s.close()
        else:
            ipv4 = socket.gethostbyname_ex(socket.getfqdn(''))[-1]
            self._ipv4 = ipv4[0]

    def update_url(self):
        self._url = 'http://' + self._ipv4 + ':8000/'

    def get_url(self):
        return self._url

    def start_server(self):
        if self.check_running_server():
            return
        self._server = ThreadingServer(("", self._port), self._handler)
        self._server.timeout = 60
        self._server.start_server()

    def stop_server(self):
        if self.check_running_server():
            try:
                self._server.stop_server()
            except AttributeError:
                print('Can\'t close the server')

    def check_running_server(self):
        a = socket.getaddrinfo("localhost", 8000, socket.AF_INET, socket.SOCK_STREAM)
        family, socktype, proto, canonname, sockaddr = a[0]
        s = socket.socket(family, socktype, proto)
        try:
            s.connect(sockaddr)
            s.close()
            return True
        except OSError:
            return False
