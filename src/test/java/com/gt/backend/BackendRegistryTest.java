package com.gt.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BackendRegistryTest {

    @Test
    @DisplayName("Should initialize with provided list")
    void shouldInitializeWithGivenBackends() {
        var initialList = List.of("http://localhost:8081", "http://localhost:8082");
        var registry = new BackendRegistry(initialList);

        assertEquals(2, registry.getAvailableBackends().size());
        assertTrue(registry.getAvailableBackends().containsAll(initialList));
    }

    @Test
    @DisplayName("Should add and remove backends dynamically")
    void shouldAddAndRemoveBackends() {
        var registry = new BackendRegistry(List.of("http://localhost:8081"));

        registry.addBackend("http://localhost:8082");
        assertEquals(2, registry.getAvailableBackends().size());

        registry.removeBackend("http://localhost:8081");
        assertEquals(1, registry.getAvailableBackends().size());
        assertEquals("http://localhost:8082", registry.getAvailableBackends().get(0));
    }

    @Test
    @DisplayName("Should return externally immutable list")
    void shouldReturnUnmodifiableList() {
        var registry = new BackendRegistry(List.of("http://localhost:8081"));
        var backends = registry.getAvailableBackends();

        assertThrows(UnsupportedOperationException.class, () -> {
            backends.add("http://localhost:9999");
        });
    }
}