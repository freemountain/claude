package org.freemountain.operator.crds;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DataStoreAccessClaimResourceDoneable extends CustomResourceDoneable<DataStoreAccessClaimResource> {
    public DataStoreAccessClaimResourceDoneable(DataStoreAccessClaimResource resource, Function<DataStoreAccessClaimResource, DataStoreAccessClaimResource> function) {
        super(resource, function);
    }
}
