package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.Collection;
import java.util.stream.Collectors;

public class ResourceUtils {

    public static <T extends HasMetadata> String inspect(T resource) {
        return resource.getKind() + ":" + resource.getMetadata().getName();
    }

    public static <T extends HasMetadata> String inspect(Collection<T> resources) {
        return resources
                .stream()
                .map(ResourceUtils::inspect)
                .collect(Collectors.joining(", "));
    }
}
