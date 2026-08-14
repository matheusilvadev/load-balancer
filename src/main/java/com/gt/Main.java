package com.gt;

import com.gt.backend.BackendRegistry;
import com.gt.handler.ProxyHandler;
import com.gt.proxy.HttpProxyClient;
import com.gt.routing.RoundRobinRoutingStrategy;
import com.gt.routing.RoutingStrategy;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;

        //Manual Configuration and Dependency Injection
        BackendRegistry registry = new BackendRegistry(List.of(
                "http://localhost:8081",
                "http://localhost:8082",
                "http://localhost:8083"
        ));

        RoutingStrategy strategy = new RoundRobinRoutingStrategy();
        HttpProxyClient proxyClient = new HttpProxyClient();

        ProxyHandler proxyHandler = new ProxyHandler(registry, strategy, proxyClient);

        // Server Bootstrap (native)
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(20)); // Thread Pool (isolated)
        server.createContext("/", proxyHandler);

        System.out.println("Modular Load Balancer running on port " + port + "...");
        server.start();
    }
}