package com.gt.routing;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinRoutingStrategy implements RoutingStrategy{
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String selectBackend(List<String> backends) {
        if (backends == null || backends.isEmpty()){
            throw new IllegalStateException("No backend available at the moment.");
        }

        int index = Math.abs(counter.getAndIncrement() % backends.size());
        return backends.get(index);
    }
}
