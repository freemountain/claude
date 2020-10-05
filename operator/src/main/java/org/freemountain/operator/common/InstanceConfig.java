package org.freemountain.operator.common;

import java.util.UUID;

public class InstanceConfig {
    private final UUID id = UUID.randomUUID();

    public String getId() {
        return id.toString();
    }
}
