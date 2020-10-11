package org.freemountain.operator.caches;


import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.Watchable;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.crds.*;
import org.freemountain.operator.events.DataStoreAccessClaimLifecycleEvent;
import org.freemountain.operator.events.DataStoreLifecycleEvent;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.freemountain.operator.events.LifecycleEvent;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DataStoreAccessClaimCacheEmitter extends CachedEmitter<DataStoreAccessClaimResource>{
    static class DataStoreAccessClaimHandler extends LifecycleClient.KubernetesEventHandler<DataStoreAccessClaimResource> implements ResourceEventHandler<DataStoreAccessClaimResource> {};

    @Outgoing(DataStoreAccessClaimLifecycleEvent.ADDRESS)
    @Broadcast
    Publisher<DataStoreAccessClaimLifecycleEvent> connect() {
        SharedIndexInformer<DataStoreAccessClaimResource> informer = getInformerFactory().sharedIndexInformerForCustomResource(CRD.DataStoreAccessClaim.CONTEXT, DataStoreAccessClaimResource.class, DataStoreAccessClaimResourceList.class, 60 *1000);
        LifecycleClient<DataStoreAccessClaimResource> client = new LifecycleClient<DataStoreAccessClaimResource>(informer, new DataStoreAccessClaimHandler());
        return super.connect(client).onItem().transform(DataStoreAccessClaimLifecycleEvent::new);
    }

}
