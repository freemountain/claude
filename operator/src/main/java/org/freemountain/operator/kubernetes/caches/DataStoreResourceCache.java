package org.freemountain.operator.kubernetes.caches;

import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.freemountain.operator.kubernetes.resources.DataStoreResource;
import org.freemountain.operator.kubernetes.resources.DataStoreResourceDoneable;
import org.freemountain.operator.kubernetes.resources.DataStoreResourceList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class DataStoreResourceCache extends ResourceCache<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> {

    private NonNamespaceOperation<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> crClient;

    public DataStoreResourceCache() {
        super(null);
    }

    @Inject
    public DataStoreResourceCache(NonNamespaceOperation<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> crClient) {
        super(crClient);
    }
}
