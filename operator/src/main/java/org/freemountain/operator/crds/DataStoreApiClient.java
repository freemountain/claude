package org.freemountain.operator.crds;

import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import org.freemountain.operator.common.CRDApiClient;
import org.freemountain.operator.common.CRDContext;

public class DataStoreApiClient
        extends CRDApiClient<
                DataStoreResource,
                DataStoreResourceList,
                DataStoreResourceDoneable,
                Resource<DataStoreResource, DataStoreResourceDoneable>> {
    public DataStoreApiClient(
            MixedOperation<
                            DataStoreResource,
                            DataStoreResourceList,
                            DataStoreResourceDoneable,
                            Resource<DataStoreResource, DataStoreResourceDoneable>>
                    typedClient,
            RawCustomResourceOperationsImpl rawClient,
            CRDContext<DataStoreResource> crd) {
        super(typedClient, rawClient, crd);
    }
}
