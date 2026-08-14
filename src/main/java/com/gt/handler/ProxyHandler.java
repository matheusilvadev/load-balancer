package com.gt.handler;

import com.gt.backend.BackendRegistry;
import com.gt.proxy.HttpProxyClient;
import com.gt.routing.RoutingStrategy;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class ProxyHandler implements HttpHandler {
    private final BackendRegistry backendRegistry;
    private final RoutingStrategy routingStrategy;
    private final HttpProxyClient proxyClient;

    public ProxyHandler(BackendRegistry backendRegistry, RoutingStrategy routingStrategy, HttpProxyClient proxyClient) {
        this.backendRegistry = backendRegistry;
        this.routingStrategy = routingStrategy;
        this.proxyClient = proxyClient;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var backends = backendRegistry.getAvailableBackends();
            String selectedBackend = routingStrategy.selectBackend(backends);

            proxyClient.forwardRequest(exchange, selectedBackend);
        } catch (IllegalStateException e) {
            byte[] response = "No backend available.".getBytes();
            exchange.sendResponseHeaders(503, response.length);
            try (var os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
}
