package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.Watchable;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.common.ResourceUtils;
import org.freemountain.operator.events.LifecycleEvent;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class ResourceCacheEmitter<T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>, R extends Resource<T, D>> {
    private static final Logger LOGGER = Logger.getLogger(ResourceCacheEmitter.class);

    @Inject
    ManagedExecutor managedExecutor;

    private final Map<String, T> cache = new ConcurrentHashMap<>();

    protected abstract Listable<L> getListClient();

    protected abstract Watchable<?, Watcher<T>> getWatchClient();

    public T get(String uid) {
        return cache.get(uid);
    }

    protected Multi<LifecycleEvent<T>> watch() {
        UnicastProcessor<LifecycleEvent<T>> buffer = UnicastProcessor.create();
        getWatchClient().watch(new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T t) {
                Optional<LifecycleType> type = LifecycleType.from(action);

                if (type.isEmpty()) {
                    buffer.onError(new IllegalArgumentException("Unknown event " + action));
                    return;
                }

                LifecycleEvent<T> event = new LifecycleEvent<T>(type.get(), t, false, false);
                buffer.onNext(event);
            }

            @Override
            public void onClose(KubernetesClientException e) {
                buffer.onError(e);
            }
        });

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
