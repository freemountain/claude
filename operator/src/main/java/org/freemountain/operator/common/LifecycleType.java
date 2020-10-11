package org.freemountain.operator.common;

import io.fabric8.kubernetes.client.Watcher;

import java.util.Optional;

public enum LifecycleType {
    ADDED,
    MODIFIED,
    DELETED;
}
