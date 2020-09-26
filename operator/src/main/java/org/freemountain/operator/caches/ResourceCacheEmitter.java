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
import org.freemountain.operator.events.LifecycleEvent;
import org.freemountain.operator.common.LifecycleType;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.freemountain.operator.common.ResourceUtils.inspect;

public abstract class ResourceCacheEmitter<T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>, R extends Resource<T, D>> {

    private static final Logger LOGGER = Logger.getLogger(ResourceCacheEmitter.class);

    private final Map<String, T> cache = new ConcurrentHashMap<>();

    protected abstract Listable<L> getListClient();

    protected abstract Watchable<?, Watcher<T>> getWatchClient();

    public T get(String uid) {
        return cache.get(uid);
    }


    protected Multi<LifecycleEvent<T>> watch() {
        Multi<LifecycleEvent<T>> initialEvents = Multi.createFrom().emitter(emitter -> {
            List<T> items = getListClient().list().getItems();

            if (items.size() > 0) {
                LifecycleEvent<T> event = new LifecycleEvent<T>(LifecycleType.ADDED, items);
                emitter.emit(event);
            }

            emitter.complete();
        });

        UnicastProcessor<LifecycleEvent<T>> buffer = UnicastProcessor.create();
        getWatchClient().watch(new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T t) {
                Optional<LifecycleType> type = LifecycleType.from(action);

                if (type.isEmpty()) {
                    buffer.onError(new IllegalArgumentException("Unknown event " + action));
                    return;
                }

                LifecycleEvent<T> event = new LifecycleEvent<T>(type.get(), t);
                buffer.onNext(event);
            }

            @Override
            public void onClose(KubernetesClientException e) {
                buffer.onError(e);
            }
        });

        Multi<LifecycleEvent<T>> watchedEvents = buffer.on().failure().invoke(error -> {
            LOGGER.errorf(error, "kubernetes resource watcher failed");
            System.exit(-1);
        });

        return Multi
                .createBy().concatenating().streams(initialEvents, watchedEvents)
                .transform().byTestingItemsWith(this::applyOnCache)
                .onItem().invoke(event -> LOGGER.debugf("LifecycleEvent %s %s", event.getType(), inspect(event.getResources())));
    }

    private Uni<Boolean> applyOnCache(LifecycleEvent<T> event) {
        if (event.getType().equals(LifecycleType.ADDED) && event.getResources().size() > 1) {
            for (T resource : event.getResources()) {
                String uid = resource.getMetadata().getUid();
                cache.put(uid, resource);
            }
            return Uni.createFrom().item(true);
        }

        T resource = event.getResource();
        String uid = resource.getMetadata().getUid();

        if (event.getType().equals(LifecycleType.ADDED) && !cache.containsKey(uid)) {
            cache.put(uid, resource);
            return Uni.createFrom().item(true);
        }

        if (event.getType().equals(LifecycleType.MODIFIED) && cache.containsKey(uid)) {
            int knownResourceVersion = Integer.parseInt(cache.get(uid).getMetadata().getResourceVersion());
            int receivedResourceVersion = Integer.parseInt(resource.getMetadata().getResourceVersion());
            if (knownResourceVersion <= receivedResourceVersion) {
                cache.put(uid, resource);
                return Uni.createFrom().item(true);
            }
        }

        if (event.getType().equals(LifecycleType.DELETED) && cache.containsKey(uid)) {
            cache.remove(uid);
            return Uni.createFrom().item(true);
        }

        LOGGER.debugf("Skipped old %s event (%s)", event.getType(), inspect(event.getResources()));
        return Uni.createFrom().item(false);
    }

}
