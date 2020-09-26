package org.freemountain.operator.events;

import org.freemountain.operator.common.LifecycleType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LifecycleEvent<T> {

    private LifecycleType type;
    private List<T> resources;

    public LifecycleEvent(LifecycleType type, T resource) {
        this.type = type;
        this.resources = Collections.singletonList(resource);
    }

    public LifecycleEvent(LifecycleType type, List<T> resources) {
        this.type = type;
        this.resources = new LinkedList<>();
        this.resources.addAll(resources);
    }

    public LifecycleType getType() {
        return type;
    }

    public List<T> getResources() {
        return resources;
    }

    public T getResource() {
        if(resources != null && resources.size() > 0) {
            return resources.get(0);
        }
        return null;
    }

}
