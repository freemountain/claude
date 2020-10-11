package org.freemountain.operator.crds;

import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import org.freemountain.operator.common.CRDApiClient;
import org.freemountain.operator.common.CRDContext;

public class DataStoreAccessClaimApiClient extends CRDApiClient<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> {
    public DataStoreAccessClaimApiClient(MixedOperation<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> typedClient, RawCustomResourceOperationsImpl rawClient, CRDContext<DataStoreAccessClaimResource> crd) {
        super(typedClient, rawClient, crd);
    }
}
