package org.freemountain.operator.events;

import org.freemountain.operator.crds.DataStoreResource;

public class DataStoreLifecycleEvent extends WithStatusLifecycleEvent<DataStoreResource> {
    public static final String ADDRESS = "lifecycle.datastore";

    public DataStoreLifecycleEvent(LifecycleEvent<DataStoreResource> other) {
        super(other);
    }
}
