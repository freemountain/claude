package org.freemountain.operator.events;

import org.freemountain.operator.common.LifecycleType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LifecycleEvent<T> {

    protected   LifecycleType type;
    protected  boolean isInitial;
    protected  boolean isLastInitial;
    protected  T resource;
    protected  T previousResource;

    public LifecycleEvent(LifecycleType tye, T resource, T previousResource) {
        this.type = tye;
        this.resource = resource;
        this.isInitial = false;
        this.isLastInitial = false;
        this.previousResource = previousResource;
    }

    public LifecycleEvent(LifecycleType tye, T resource) {
        this.type = tye;
        this.resource = resource;
        this.isInitial = false;
        this.isLastInitial = false;
        this.previousResource = null;
    }

    public LifecycleEvent(LifecycleEvent<T> other) {
        this.type = other.type;
        this.resource = other.resource;
        this.isInitial = other.isInitial;
        this.isLastInitial = other.isLastInitial;
        this.previousResource = other.previousResource;
    }

    public LifecycleType getType() {
        return type;
    }

    public T getResource() {
        return resource;
    }

    public T getPreviousResource() { return previousResource; }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isLastInitial() {
        return isLastInitial;
    }


    public  class Builder<T> extends LifecycleEvent<T> {
        public Builder(LifecycleEvent<T> other) {
            super(other);
        }

        public LifecycleEvent<T> build() {
            return new LifecycleEvent<>(this);
        }


        public void setType(LifecycleType type) {
            this.type = type;
        }

        public void setInitial(boolean initial) {
            isInitial = initial;
        }

        public void setLastInitial(boolean lastInitial) {
            isLastInitial = lastInitial;
        }

        public void setResource(T resource) {
            this.resource = resource;
        }

        public void setPreviousResource(T previousResource) {
            this.previousResource = previousResource;
        }

    }
}
