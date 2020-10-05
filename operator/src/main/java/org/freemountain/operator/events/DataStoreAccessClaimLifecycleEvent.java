package org.freemountain.operator.events;

import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.crds.DataStoreAccessClaimResource;

import java.util.List;

public class DataStoreAccessClaimLifecycleEvent extends LifecycleEvent<DataStoreAccessClaimResource>{
    public static final String ADDRESS = "lifecycle.datastoreaccessclaim";

    public DataStoreAccessClaimLifecycleEvent(LifecycleEvent<DataStoreAccessClaimResource> other) {
        super(other);
    }
}
