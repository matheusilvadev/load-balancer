package com.gt.mockserver;

import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MockBackendServer {
    private final int port;
    private final String serverId;
    private HttpServer server;

    public MockBackendServer(int port, String serverId) {
        this.port = port;
        this.serverId = serverId;
    }

    public void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            String response = "Response from " + serverId + " (Port " + port + ")";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        server.start();
        System.out.println("Backend [" + serverId + "] running in port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
