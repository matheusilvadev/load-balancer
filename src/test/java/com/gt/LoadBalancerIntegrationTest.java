package com.gt;

import com.gt.backend.BackendRegistry;
import com.gt.handler.ProxyHandler;
import com.gt.mockserver.MockBackendServer;
import com.gt.proxy.HttpProxyClient;
import com.gt.routing.RoundRobinRoutingStrategy;
import com.gt.routing.RoutingStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadBalancerIntegrationTest {

    private MockBackendServer backend1;
    private MockBackendServer backend2;
    private com.sun.net.httpserver.HttpServer loadBalancerServer;
    private HttpClient client;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Start Mock Backends
        backend1 = new MockBackendServer(8081, "Backend-A");
        backend2 = new MockBackendServer(8082, "Backend-B");
        backend1.start();
        backend2.start();

        // 2. Configure the Load Balancer
        BackendRegistry registry = new BackendRegistry(List.of(
                "http://localhost:8081",
                "http://localhost:8082"
        ));

        RoutingStrategy strategy = new RoundRobinRoutingStrategy();
        HttpProxyClient proxyClient = new HttpProxyClient();
        ProxyHandler proxyHandler = new ProxyHandler(registry, strategy, proxyClient);

        loadBalancerServer = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8080), 0);
        loadBalancerServer.createContext("/", proxyHandler);
        loadBalancerServer.start();

        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (loadBalancerServer != null) loadBalancerServer.stop(0);
        if (backend1 != null) backend1.stop();
        if (backend2 != null) backend2.stop();
    }

    @Test
    @DisplayName("Should alternate traffic between Backend-A and Backend-B")
    void shouldBalanceLoadBetweenBackends() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/"))
                .GET()
                .build();

        // Request 1 -> Should go to Backend-A (8081)
        HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Request 1 received: " + response1.body());
        assertEquals("Response from Backend-A (Port 8081)", response1.body());

        // Request 2 -> Should go to Backend-B (8082)
        HttpResponse<String> response2 = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Request 2 received: " + response2.body());
        assertEquals("Response from Backend-B (Port 8082)", response2.body());

        // Request 3 -> Should go back to Backend-A (8081)
        HttpResponse<String> response3 = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Request 3 received: " + response3.body());
        assertEquals("Response from Backend-A (Port 8081)", response3.body());
    }
}
