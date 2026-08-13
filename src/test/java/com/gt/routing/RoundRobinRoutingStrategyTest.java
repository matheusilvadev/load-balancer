package com.gt.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinRoutingStrategyTest {

    private RoundRobinRoutingStrategy strategy;
    private List<String> backends;

    @BeforeEach
    void setUp(){
        strategy = new RoundRobinRoutingStrategy();
        backends = List.of("http://localhost:8081", "http://localhost:8082", "http://localhost:8083");
    }

    @Test
    @DisplayName("Should alternate between servers in correct order (Round Robin)")
    void shouldRotateBackendSequentially(){
        assertEquals("http://localhost:8081", strategy.selectBackend(backends));
        assertEquals("http://localhost:8082", strategy.selectBackend(backends));
        assertEquals("http://localhost:8083", strategy.selectBackend(backends));
        //Should wrap around to start of list (circular effect)
        assertEquals("http://localhost:8081", strategy.selectBackend(backends));
    }

    @Test
    @DisplayName("Should throw exception if backend list is empty")
    void shouldThrowExceptionWhenBackendsIsEmpty() {
        assertThrows(IllegalStateException.class, () -> {
            strategy.selectBackend(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when backend list is null")
    void shouldThrowExceptionWhenBackendsIsNull() {
        assertThrows(IllegalStateException.class, () -> {
            strategy.selectBackend(null);
        });
    }
}