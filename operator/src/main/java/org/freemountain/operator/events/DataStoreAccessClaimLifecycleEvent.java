package org.freemountain.operator.events;

import org.freemountain.operator.crds.DataStoreAccessClaimResource;

public class DataStoreAccessClaimLifecycleEvent
        extends WithStatusLifecycleEvent<DataStoreAccessClaimResource> {
    public static final String ADDRESS = "lifecycle.datastoreaccessclaim";

    public DataStoreAccessClaimLifecycleEvent(LifecycleEvent<DataStoreAccessClaimResource> other) {
        super(other);
    }
}
