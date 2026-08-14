package com.gt.handler;

import com.gt.backend.BackendRegistry;
import com.gt.proxy.HttpProxyClient;
import com.gt.routing.RoutingStrategy;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProxyHandlerTest {

    @Mock
    private BackendRegistry backendRegistry;

    @Mock
    private RoutingStrategy routingStrategy;

    @Mock
    private HttpProxyClient proxyClient;

    @Mock
    private HttpExchange exchange;

    @Test
    @DisplayName("Should forward request to selected backend when servers are available")
    void shouldForwardRequestWhenBackendIsAvailable() throws IOException {
        String targetBackend = "http://localhost:8081";
        List<String> backends = List.of(targetBackend);

        when(backendRegistry.getAvailableBackends()).thenReturn(backends);
        when(routingStrategy.selectBackend(backends)).thenReturn(targetBackend);

        ProxyHandler proxyHandler = new ProxyHandler(backendRegistry, routingStrategy, proxyClient);
        proxyHandler.handle(exchange);

        verify(proxyClient, times(1)).forwardRequest(exchange, targetBackend);
    }

    @Test
    @DisplayName("Should respond with HTTP 503 Service Unavailable if no backend is available")
    void shouldReturn503WhenNoBackendsAvailable() throws IOException {
        when(backendRegistry.getAvailableBackends()).thenReturn(Collections.emptyList());
        when(routingStrategy.selectBackend(Collections.emptyList()))
                .thenThrow(new IllegalStateException("No backend available"));

        ByteArrayOutputStream responseBodyStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(responseBodyStream);

        ProxyHandler proxyHandler = new ProxyHandler(backendRegistry, routingStrategy, proxyClient);
        proxyHandler.handle(exchange);

        verify(exchange, times(1)).sendResponseHeaders(eq(503), anyLong());

        String responseText = responseBodyStream.toString();
        assertTrue(responseText.contains("503 Service Unavailable"));

        verifyNoInteractions(proxyClient);
    }

}