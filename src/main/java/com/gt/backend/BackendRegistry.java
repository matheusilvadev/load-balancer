package com.gt.backend;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BackendRegistry {
    private final List<String> backends = new CopyOnWriteArrayList<>();

    public BackendRegistry(List<String> initialBackends){
        this.backends.addAll(initialBackends);
    }

    public List<String> getAvailableBackends(){
        return List.copyOf(backends);
    }

    public void addBackend(String backendUrl){
        backends.add(backendUrl);
    }

    public void removeBackend(String backendUrl){
        backends.remove(backendUrl);
    }
}
