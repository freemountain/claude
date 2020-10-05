package org.freemountain.operator.providers;

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.freemountain.operator.common.Constants;
import org.freemountain.operator.common.InstanceConfig;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class KubernetesConfigProvider {
    private static final String SCOPE = "Namespaced";
    private static final String VERSION = "v1alpha1";

    @ConfigProperty(name = "namespace")
    Optional<String> namespaceConfigProperty;

    @Produces
    @Singleton
    @Named("namespace")
    String findNamespace() throws IOException {
        if(namespaceConfigProperty.isEmpty()) {
            return new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace")));
        }

        return namespaceConfigProperty.get();
    }

    @Produces
    @Singleton
    InstanceConfig getInstanceConfig() {
        return new InstanceConfig();
    }

}
