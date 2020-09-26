package org.freemountain.operator.caches;

import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.Watchable;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.freemountain.operator.events.DataStoreLifecycleEvent;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.crds.DataStoreResourceDoneable;
import org.freemountain.operator.crds.DataStoreResourceList;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class DataStoreCacheEmitter extends ResourceCacheEmitter<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> {
    @Inject
    NonNamespaceOperation<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> client;

    public NonNamespaceOperation<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> getClient() {
        return client;
    }

    @Override
    protected Listable<DataStoreResourceList> getListClient() {
        return client;
    }

    @Override
    protected Watchable<?, Watcher<DataStoreResource>> getWatchClient() {
        return client;
    }

    @Outgoing(DataStoreLifecycleEvent.ADDRESS)
    Publisher<DataStoreLifecycleEvent> connect() {
        return watch().onItem().apply(event -> new DataStoreLifecycleEvent(event.getType(), event.getResources()));
    }
}
