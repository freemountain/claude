package org.freemountain.operator.caches;

import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.freemountain.operator.common.CRDContext;
import org.freemountain.operator.crds.*;
import org.freemountain.operator.events.DataStoreAccessClaimLifecycleEvent;
import org.reactivestreams.Publisher;

@ApplicationScoped
public class DataStoreAccessClaimCacheEmitter extends CachedEmitter<DataStoreAccessClaimResource> {
    static class DataStoreAccessClaimHandler
            extends LifecycleClient.KubernetesEventHandler<DataStoreAccessClaimResource>
            implements ResourceEventHandler<DataStoreAccessClaimResource> {}
    ;

    @Inject CRDContext<DataStoreAccessClaimResource> ctx;

    @Outgoing(DataStoreAccessClaimLifecycleEvent.ADDRESS)
    @Broadcast
    Publisher<DataStoreAccessClaimLifecycleEvent> connect() {
        SharedIndexInformer<DataStoreAccessClaimResource> informer =
                getInformerFactory()
                        .sharedIndexInformerForCustomResource(
                                ctx.getConfig(),
                                DataStoreAccessClaimResource.class,
                                DataStoreAccessClaimResourceList.class,
                                60 * 1000);
        LifecycleClient<DataStoreAccessClaimResource> client =
                new LifecycleClient<DataStoreAccessClaimResource>(
                        informer, new DataStoreAccessClaimHandler());
        return super.connect(client).onItem().transform(DataStoreAccessClaimLifecycleEvent::new);
    }
}
