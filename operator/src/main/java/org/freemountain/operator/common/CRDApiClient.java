package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;

public class CRDApiClient<T extends HasMetadata & HasBaseStatus, L, D, R extends Resource<T, D>> {

    private final MixedOperation<T,L,D, R> typedClient;
    private final RawCustomResourceOperationsImpl rawClient;
    private final CRDContext<T> crd;

    public CRDApiClient(MixedOperation<T, L, D, R> typedClient, RawCustomResourceOperationsImpl rawClient, CRDContext<T> crd) {
        this.typedClient = typedClient;
        this.rawClient = rawClient;
        this.crd = crd;
    }

    public CRDContext<T> crd() {
        return crd;
    }

    public MixedOperation<T,L,D, R> typed() {
        return typedClient;
    }

    public RawCustomResourceOperationsImpl raw() {
        return rawClient;
    }

    public StatusOperation<T> status() {
        return new StatusOperation<T>(typedClient, crd);
    }
}
