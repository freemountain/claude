package org.freemountain.operator.providers;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.CRDContext;
import org.freemountain.operator.crds.*;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

public class KubernetesClientProvider {
    @Produces
    @Singleton
    KubernetesClient newClient(@Named("namespace") String namespace) {
        return new DefaultKubernetesClient().inNamespace(namespace);
    }

    @Produces
    @Singleton
    DataStoreApiClient makeDataStoreClient(
            KubernetesClient defaultClient,
            CRDContext<DataStoreResource> ctx

    ) {
        KubernetesDeserializer.registerCustomKind(ctx.getApiVersion(), ctx.getConfig().getKind(), ctx.getCRDClass());

        var typedClient = defaultClient.customResources(ctx.getConfig(), DataStoreResource.class, DataStoreResourceList.class, DataStoreResourceDoneable.class);
        var rawClient = defaultClient.customResource(ctx.getConfig());

        return new DataStoreApiClient(typedClient, rawClient, ctx);
    }

    @Produces
    @Singleton
    DataStoreAccessClaimApiClient makeDataStoreAccessClaimClient(
            KubernetesClient defaultClient,
            CRDContext<DataStoreAccessClaimResource> ctx
    ) {
        KubernetesDeserializer.registerCustomKind(ctx.getApiVersion(), ctx.getConfig().getKind(), ctx.getCRDClass());

        var typedClient = defaultClient.customResources(ctx.getConfig(), DataStoreAccessClaimResource.class, DataStoreAccessClaimResourceList.class, DataStoreAccessClaimResourceDoneable.class);
        var rawClient = defaultClient.customResource(ctx.getConfig());

        return new DataStoreAccessClaimApiClient(typedClient, rawClient, ctx);
    }


}
