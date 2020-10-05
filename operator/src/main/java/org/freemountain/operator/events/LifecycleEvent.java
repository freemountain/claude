package org.freemountain.operator.events;

import org.freemountain.operator.common.LifecycleType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LifecycleEvent<T> {

    private final LifecycleType type;
    private final boolean isInitial;
    private final boolean isLastInitial;
    private final T resource;

    public LifecycleEvent(LifecycleType tye, T resource, boolean isInitial, boolean isLastInitial) {
        this.type = tye;
        this.resource = resource;
        this.isInitial = isInitial;
        this.isLastInitial = isInitial && isLastInitial;
    }

    public LifecycleEvent(LifecycleEvent<T> other) {
        this.type = other.type;
        this.resource = other.resource;
        this.isInitial = other.isInitial;
        this.isLastInitial = other.isLastInitial;
    }

    public LifecycleType getType() {
        return type;
    }

    public T getResource() {
        return resource;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isLastInitial() {
        return isLastInitial;
    }
}
