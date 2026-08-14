package com.gt.proxy;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpProxyClient {
    private final HttpClient httpClient;

    public HttpProxyClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public void forwardRequest(HttpExchange exchange, String targetBackend) throws IOException {
        String targetUrl = targetBackend + exchange.getRequestURI();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .method(exchange.getRequestMethod(), HttpRequest.BodyPublishers.ofInputStream(exchange::getRequestBody))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            exchange.sendResponseHeaders(response.statusCode(), response.body().length);
            try (OutputStream os = exchange.getResponseBody()){
                os.write(response.body());
            }
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            sendError(exchange, 500, "Internal Server Error.");
        } catch (Exception e){
            sendError(exchange, 502, "Bad Gateway" + targetBackend);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
