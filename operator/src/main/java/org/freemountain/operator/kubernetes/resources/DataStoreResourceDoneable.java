package org.freemountain.operator.kubernetes.resources;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DataStoreResourceDoneable extends CustomResourceDoneable<DataStoreResource> {

    public DataStoreResourceDoneable(DataStoreResource resource, Function<DataStoreResource, DataStoreResource> function) {
        super(resource, function);
    }
}
