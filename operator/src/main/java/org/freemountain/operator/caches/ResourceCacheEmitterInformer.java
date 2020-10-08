package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.common.ResourceUtils;
import org.freemountain.operator.events.LifecycleEvent;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ResourceCacheEmitterInformer<T extends HasMetadata> {
    private static final Logger LOGGER = Logger.getLogger(ResourceCacheEmitterInformer.class);

    @Inject
    KubernetesClient client;

    @Inject
    ManagedExecutor managedExecutor;

    private final Map<String, T> cache = new ConcurrentHashMap<>();

    protected abstract SharedIndexInformer<T> createInformer();
    protected abstract ResourceEventHandler<T> createHandler(UnicastProcessor<LifecycleEvent<T>> buffer);

    public Optional<T> get(String uid) {
        return Optional.ofNullable(cache.get(uid));
    }

    public Collection<T> values() {
        return cache.values();
    }


    protected SharedInformerFactory informerFactory() {
        return client.informers(managedExecutor);
    }

    protected Multi<LifecycleEvent<T>> watch() {
        UnicastProcessor<LifecycleEvent<T>> buffer = UnicastProcessor.create();
        var informer = createInformer();
        informer.addEventHandler(createHandler(buffer));
        informer.run();

        return buffer
                .on().failure().invoke(error -> {
                    LOGGER.errorf(error, "kubernetes resource watcher failed");
                    System.exit(-1);
                })
                .transform().byTestingItemsWith(this::applyOnCache)
                .onItem().invoke(event -> LOGGER.debugf("LifecycleEvent %s %s", event.getType(), ResourceUtils.inspect(event.getResource())));
    }

    private Uni<Boolean> applyOnCache(LifecycleEvent<T> event) {
        T resource = event.getResource();
        String uid = resource.getMetadata().getUid();

        if (event.getType().equals(LifecycleType.ADDED) && !cache.containsKey(uid)) {
            cache.put(uid, event.getResource());
            return Uni.createFrom().item(true);
        }

        if (event.getType().equals(LifecycleType.MODIFIED) && cache.containsKey(uid)) {
            cache.put(uid, resource);
            return Uni.createFrom().item(true);
        }

        if (event.getType().equals(LifecycleType.DELETED) && cache.containsKey(uid)) {
            cache.remove(uid);
            return Uni.createFrom().item(true);
        }

        LOGGER.debugf("Skipped invalid %s event (%s)", event.getType(), ResourceUtils.inspect(event.getResource()));
        return Uni.createFrom().item(false);
    }

}
