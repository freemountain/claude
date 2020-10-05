package org.freemountain.operator.caches;


import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.Watchable;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.freemountain.operator.events.DataStoreAccessClaimLifecycleEvent;
import org.freemountain.operator.crds.DataStoreAccessClaimResource;
import org.freemountain.operator.crds.DataStoreAccessClaimResourceDoneable;
import org.freemountain.operator.crds.DataStoreAccessClaimResourceList;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DataStoreAccessClaimCacheEmitter extends ResourceCacheEmitter<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> {
    @Inject
    NonNamespaceOperation<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> client;

     NonNamespaceOperation<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> getClient() {
        return client;
    }

    @Override
    protected Listable<DataStoreAccessClaimResourceList> getListClient() {
        return client;
    }

    @Override
    protected Watchable<?, Watcher<DataStoreAccessClaimResource>> getWatchClient() {
        return client;
    }

    @Outgoing(DataStoreAccessClaimLifecycleEvent.ADDRESS)
    Publisher<DataStoreAccessClaimLifecycleEvent> connect() {
        return watch().onItem().apply(DataStoreAccessClaimLifecycleEvent::new);
    }
}
