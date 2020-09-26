package org.freemountain.operator.providers;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.freemountain.operator.crds.*;

import javax.enterprise.inject.Produces;
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
    @Named("rawDataStoreClient")
    RawCustomResourceOperationsImpl getRawDataStoreClient(
            @Named("dataStoreCrdContext") CustomResourceDefinitionContext dataStoreCrdContext,
            KubernetesClient defaultClient
    ) {
        return defaultClient.customResource(dataStoreCrdContext);
    }

    @Produces
    @Singleton
    NonNamespaceOperation<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> makeDataStoreClient(
            KubernetesClient defaultClient,
            @Named("dataStoreCrdContext") CustomResourceDefinitionContext dataStoreCrdContext
    ) {

        String apiVersion = dataStoreCrdContext.getGroup() + "/" + dataStoreCrdContext.getVersion();
        KubernetesDeserializer.registerCustomKind(apiVersion, dataStoreCrdContext.getKind(), DataStoreResource.class);

        return defaultClient.customResources(dataStoreCrdContext, DataStoreResource.class, DataStoreResourceList.class, DataStoreResourceDoneable.class);
    }

    @Produces
    @Singleton
    NonNamespaceOperation<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> makeDataStoreAccessClaimClient(
            KubernetesClient defaultClient,
            @Named("dataStoreAccessClaimCrdContext") CustomResourceDefinitionContext crdContext
    ) {

        String apiVersion = crdContext.getGroup() + "/" + crdContext.getVersion();
        KubernetesDeserializer.registerCustomKind(apiVersion, crdContext.getKind(), DataStoreAccessClaimResource.class);

        return defaultClient.customResources(crdContext, DataStoreAccessClaimResource.class, DataStoreAccessClaimResourceList.class, DataStoreAccessClaimResourceDoneable.class);
    }


}
