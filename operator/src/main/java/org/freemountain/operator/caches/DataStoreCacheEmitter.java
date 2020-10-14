package org.freemountain.operator.caches;

import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.freemountain.operator.common.CRDContext;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.crds.DataStoreResourceList;
import org.freemountain.operator.events.DataStoreLifecycleEvent;
import org.reactivestreams.Publisher;

@ApplicationScoped
public class DataStoreCacheEmitter extends CachedEmitter<DataStoreResource> {
    static class DataStoreHandler extends LifecycleClient.KubernetesEventHandler<DataStoreResource>
            implements ResourceEventHandler<DataStoreResource> {}
    ;

    @Inject CRDContext<DataStoreResource> ctx;

    @Outgoing(DataStoreLifecycleEvent.ADDRESS)
    @Broadcast
    Publisher<DataStoreLifecycleEvent> connect() {
        SharedIndexInformer<DataStoreResource> informer =
                getInformerFactory()
                        .sharedIndexInformerForCustomResource(
                                ctx.getConfig(),
                                DataStoreResource.class,
                                DataStoreResourceList.class,
                                60 * 1000);
        LifecycleClient<DataStoreResource> client =
                new LifecycleClient<DataStoreResource>(informer, new DataStoreHandler());
        return super.connect(client).onItem().transform(DataStoreLifecycleEvent::new);
    }
}
