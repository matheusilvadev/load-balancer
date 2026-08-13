package com.gt.routing;

import java.util.List;

public interface RoutingStrategy {
    String selectBackend(List<String> backends);
}
