package org.freemountain.operator.common;

import io.fabric8.kubernetes.client.Watcher;

import java.util.Optional;

public enum LifecycleType {
    ADDED,
    MODIFIED,
    DELETED;


    public static Optional<LifecycleType> from(Watcher.Action action) {
        switch (action) {
            case ADDED: return Optional.of(ADDED);
            case MODIFIED: return Optional.of(MODIFIED);
            case DELETED: return Optional.of(DELETED);
            default: return Optional.empty();
        }

    }
}
