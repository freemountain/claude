package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceUtils {

    public static <T extends HasMetadata> String inspect(T resource) {
        return resource.getKind() + ":" + resource.getMetadata().getName();
    }

    public static <T extends HasMetadata> String inspect(Collection<T> resources) {
        return resources.stream().map(ResourceUtils::inspect).collect(Collectors.joining(", "));
    }

    public static Optional<OwnerReference> getOwningCRD(HasMetadata resource, CRDContext<?> crd) {
        List<OwnerReference> owners = resource.getMetadata().getOwnerReferences();
        owners = owners == null ? Collections.emptyList() : owners;

        return owners.stream().filter(crd::isResource).findFirst();
    }
}
