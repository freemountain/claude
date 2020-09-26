package org.freemountain.operator.events;

import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.crds.DataStoreResource;

import java.util.List;

public class DataStoreLifecycleEvent extends LifecycleEvent<DataStoreResource> {
    public static final String ADDRESS = "lifecycle.datastore";

    public DataStoreLifecycleEvent(LifecycleType type, List<DataStoreResource> resources) {
        super(type, resources);
    }
}
