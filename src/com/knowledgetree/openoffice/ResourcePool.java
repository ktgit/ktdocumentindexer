package com.knowledgetree.openoffice;

import java.util.Stack;

public abstract class ResourcePool {

    String name;
    Stack availableResources, busyResources;

    public ResourcePool(String name) {
        this.name = name;
        availableResources = new Stack();
        busyResources      = new Stack();
    }

    protected abstract Object createNewResource();

    public synchronized Object getResource() {
        Object result = null;
        if (availableResources.empty()) {
            result = createNewResource();
        } else
            result = availableResources.pop();
        if (result != null) busyResources.push(result);
        return result;
    }

    public synchronized void releaseResource(Object resource) {
        if (resource != null)
            if (busyResources.remove(resource))
                availableResources.push(resource);
    }
}
