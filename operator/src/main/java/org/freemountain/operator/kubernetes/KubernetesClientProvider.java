package org.freemountain.operator.kubernetes;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.freemountain.operator.kubernetes.resources.*;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KubernetesClientProvider {
    @Produces
    @Singleton
    @Named("namespace")
    private String findNamespace() throws IOException {
        return new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace")));
    }

    @Produces
    @Singleton
    KubernetesClient newClient(@Named("namespace") String namespace) {
        return new DefaultKubernetesClient().inNamespace(namespace);
    }

    @Produces
    @Singleton
    NonNamespaceOperation<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> makeDataStoreClient(KubernetesClient defaultClient, @Named("namespace") String namespace) {

        KubernetesDeserializer.registerCustomKind("instana.com/v1alpha1", "DataStore", DataStoreResource.class);

        CustomResourceDefinition crd = defaultClient
                .customResourceDefinitions()
                .list()
                .getItems()
                .stream()
                .filter(d -> "datastores.instana.com".equals(d.getMetadata().getName()))
                .findAny()
                .orElseThrow(
                        () -> new RuntimeException("Deployment error: Custom resource definition datastores.instana.com not found."));

        return defaultClient
                .customResource(crd, DataStoreResource.class, DataStoreResourceList.class, DataStoreResourceDoneable.class)
                .inNamespace(namespace);
    }

    @Produces
    @Singleton
    NonNamespaceOperation<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> makeDataStoreAccessClaimClient(KubernetesClient defaultClient, @Named("namespace") String namespace) {

        KubernetesDeserializer.registerCustomKind("instana.com/v1alpha1", "DataStoreAccessClaim", DataStoreAccessClaimResource.class);

        CustomResourceDefinition crd = defaultClient
                .customResourceDefinitions()
                .list()
                .getItems()
                .stream()
                .filter(d -> "datastoreaccessclaims.instana.com".equals(d.getMetadata().getName()))
                .findAny()
                .orElseThrow(
                        () -> new RuntimeException("Deployment error: Custom resource definition datastoreaccessclaims.instana.com not found."));

        return defaultClient
                .customResource(crd, DataStoreAccessClaimResource.class, DataStoreAccessClaimResourceList.class, DataStoreAccessClaimResourceDoneable.class)
                .inNamespace(namespace);
    }


}
