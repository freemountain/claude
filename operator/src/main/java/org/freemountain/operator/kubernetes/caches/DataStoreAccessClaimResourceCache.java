package org.freemountain.operator.kubernetes.caches;


import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.freemountain.operator.kubernetes.resources.DataStoreAccessClaimResource;
import org.freemountain.operator.kubernetes.resources.DataStoreAccessClaimResourceDoneable;
import org.freemountain.operator.kubernetes.resources.DataStoreAccessClaimResourceList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DataStoreAccessClaimResourceCache extends ResourceCache<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> {

    private NonNamespaceOperation<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> crClient;

    public DataStoreAccessClaimResourceCache() {
        super(null);
    }

    @Inject
    public DataStoreAccessClaimResourceCache(NonNamespaceOperation<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> crClient) {
        super(crClient);
    }
}
