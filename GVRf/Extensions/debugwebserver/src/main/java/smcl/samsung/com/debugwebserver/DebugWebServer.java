package smcl.samsung.com.debugwebserver;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.WebSocket.StringCallback;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServer.WebSocketRequestCallback;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.gearvrf.GVRContext;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class creates a web server that serves webpage with a debug console for GVRf. It uses
 * {@link org.gearvrf.debug.cli.ConsoleIO} internally to forward commands from the web-console to
 * GVRf. After creating the instance, call {@link DebugWebServer#listen(int)} to start the webserver.
 * You can enter http://\<hostname for device running the GVRf app\>:\<port\> in your browser to
 * access the console.
 *
 */
public class DebugWebServer {
    private static final String TAG = DebugWebServer.class.getSimpleName();
    private static final String NOT_READY_RESPONSE = "Server Not Ready";
    private static final String DEFAULT_PATH = "/";
    private static final String WEBSOCKET_PATH = "/commands";
    private static final int MAX_CLIENTS = 3;
    private static final byte[] LINE_ENDING = new byte[]{0x0d, 0x0a};

    private AsyncHttpServer server;
    private ExecutorService executorService;
    private List<WebSocketConnection> webSocketConnections;
    private int fileSize = 0;
    private final Object lock = new Object();

    /**
     * Creates an instance of the {@link DebugWebServer}. After creating the instance, a call to
     * {@link DebugWebServer#listen(int)} is required to start the server and listen on a particular
     * port number.
     * @param gvrContext the instance of {@link GVRContext} associated with the app.
     */
    public DebugWebServer(final GVRContext gvrContext) {
        webSocketConnections = new ArrayList<WebSocketConnection>(MAX_CLIENTS);
        server = new AsyncHttpServer();
        executorService = Executors.newFixedThreadPool(MAX_CLIENTS);
        //Have to read the whole steam to determine file size.
        executorService.submit(new ReadFileSize(gvrContext.getContext().getResources()
                .openRawResource(R.raw.index)));
        server.get(DEFAULT_PATH, new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse
                    response) {
                int localFileSize;
                synchronized (lock) {
                    localFileSize = fileSize;
                }
                if (localFileSize == 0) {
                    response.send(NOT_READY_RESPONSE);
                } else {
                    try {
                        InputStream is = gvrContext.getContext().getResources().openRawResource(R
                                .raw.index);
                        response.sendStream(is, localFileSize);
                        is.close();
                    } catch (IOException e) {
                        Log.d(TAG, "", e);
                    }
                }
            }
        });

        server.websocket(WEBSOCKET_PATH, new WebSocketRequestCallback() {
            @Override
            public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
                if (webSocketConnections.size() < MAX_CLIENTS) {
                    WebSocketConnection webSocketConnection = new WebSocketConnection(webSocket,
                            webSocketConnections);
                    Log.d(TAG, "Starting new debug shell");
                    executorService.submit(new DebugShellRunnable(gvrContext, webSocketConnection
                            .getInputStream(), webSocketConnection.getOutputStream()));
                } else {
                    Log.d(TAG, "Max websocket connections already established");
                    webSocket.close();
                }
            }
        });
    }

    /**
     * Start the web server and listen on a particular port number
     * @param portnumber the port number to listen on
     */
    public void listen(int portnumber) {
        server.listen(portnumber);
    }

    /**
     * Stop the webserver.
     */
    public void stop() {
        server.stop();
        for (WebSocketConnection connection : webSocketConnections) {
            connection.close();
        }
    }

    private static class WebSocketConnection implements StringCallback, CompletedCallback {

        private WebSocket websocket;
        private PipedOutputStream pipedOutputStream;
        private WebSocketOutputStream webSocketOutputStream;
        private PipedInputStream pipedInputStream;
        private List<WebSocketConnection> webSocketConnections;

        WebSocketConnection(WebSocket websocket, List<WebSocketConnection> webSocketConnections) {
            this.websocket = websocket;
            this.webSocketConnections = webSocketConnections;
            websocket.setStringCallback(this);
            websocket.setClosedCallback(this);
            webSocketOutputStream = new WebSocketOutputStream(websocket);
            pipedOutputStream = new PipedOutputStream();
            try {
                pipedInputStream = new PipedInputStream(pipedOutputStream);
            } catch (IOException e) {
                Log.d(TAG, "Could not open inputstream", e);
                return;
            }
            webSocketConnections.add(this);
        }

        @Override
        public void onCompleted(Exception ex) {
            Log.d(TAG, "Closing websocket");
            cleanUp();
        }

        @Override
        public void onStringAvailable(String s) {
            if (pipedOutputStream != null) {
                try {
                    pipedOutputStream.write(s.getBytes());
                    pipedOutputStream.write(LINE_ENDING);
                    pipedOutputStream.flush();
                } catch (IOException e) {
                    Log.d(TAG, "Cannot write to output stream");
                }
            }
        }

        OutputStream getOutputStream() {
            return webSocketOutputStream;
        }

        InputStream getInputStream() {
            return pipedInputStream;
        }

        void close() {
            websocket.close();
            cleanUp();
        }

        private void cleanUp() {
            try {
                webSocketOutputStream.close();
                pipedInputStream.close();
                pipedOutputStream.close();
                webSocketConnections.remove(this);
            } catch (IOException e) {
                Log.e(TAG, "Could not close websocket streams", e);
            }
        }
    }

    private static class WebSocketOutputStream extends OutputStream {
        private WebSocket webSocket;

        WebSocketOutputStream(WebSocket webSocket) {
            if (webSocket == null || !webSocket.isOpen()) {
                throw new IllegalArgumentException("WebSocketOutputStream needs an open websocket");
            }
            this.webSocket = webSocket;
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
            webSocket.send(new String(buffer, offset, count));
        }

        @Override
        public void write(byte[] buffer) throws IOException {
            webSocket.send(new String(buffer));
        }

        @Override
        public void write(int oneByte) throws IOException {
            webSocket.send(new byte[]{(byte) oneByte});
        }
    }

    private class ReadFileSize implements Runnable {
        InputStream is;

        ReadFileSize(InputStream inputStream) {
            is = inputStream;
        }

        @Override
        public void run() {
            int bytesRead = 0;
            try {
                while (is.read() != -1) {
                    bytesRead++;
                }
                synchronized (lock) {
                    fileSize = bytesRead;
                }
            } catch (IOException e) {
                Log.d(TAG, "" + e.getMessage());
            }
        }
    }
}
