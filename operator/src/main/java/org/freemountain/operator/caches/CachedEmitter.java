package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.smallrye.mutiny.Multi;
import javax.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.freemountain.operator.events.LifecycleEvent;

public class CachedEmitter<T extends HasMetadata> extends LifecycleCache<T> {

    @Inject ManagedExecutor managedExecutor;

    @Inject KubernetesClient kubernetesClient;

    protected SharedInformerFactory getInformerFactory() {
        return kubernetesClient.informers(managedExecutor);
    }

    Multi<LifecycleEvent<T>> connect(LifecycleClient<T> client) {
        return client.connect().transform().byTestingItemsWith(this::applyOnCache);
    }
}
