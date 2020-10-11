package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.smallrye.mutiny.Uni;
import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.common.ResourceUtils;
import org.freemountain.operator.events.LifecycleEvent;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LifecycleCache<T extends HasMetadata> {
    private static final Logger LOGGER = Logger.getLogger(LifecycleCache.class);

    private final Map<String, T> cache = new ConcurrentHashMap<>();
    private final Map<String, T> readonlyCache = Collections.unmodifiableMap(cache);

    protected synchronized Uni<Boolean> applyOnCache(LifecycleEvent<T> event) {
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

    public Optional<T> get(String uuid) {
        return Optional.ofNullable(readonlyCache.get(uuid));
    }

    public Collection<T> values() {
        return readonlyCache.values();
    }


}
