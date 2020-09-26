package org.freemountain.operator.providers;

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KubernetesConfigProvider {
    private static final String API_GROUP = "instana.com";
    private static final String SCOPE = "Namespaced";
    private static final String VERSION = "v1alpha1";

    @Produces
    @Singleton
    @Named("namespace")
    String findNamespace() throws IOException {
        return new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace")));
    }

    @Produces
    @Named("dataStoreCrdContext")
    CustomResourceDefinitionContext getDataStoreCrdContext() {
        String plural= "datastores";
        return new CustomResourceDefinitionContext.Builder()
                .withName(plural + "." + API_GROUP)
                .withGroup(API_GROUP)
                .withScope(SCOPE)
                .withVersion(VERSION)
                .withKind("DataStore")
                .withPlural(plural)
                .build();
    }

    @Produces
    @Named("dataStoreAccessClaimCrdContext")
    CustomResourceDefinitionContext getDataStoreAccessClaimCrdContext() {
        String plural= "datastoreaccessclaims";
        return new CustomResourceDefinitionContext.Builder()
                .withName(plural + "." + API_GROUP)
                .withGroup(API_GROUP)
                .withScope(SCOPE)
                .withVersion(VERSION)
                .withKind("DataStoreAccessClaim")
                .withPlural(plural)
                .build();
    }


}
