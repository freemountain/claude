package org.freemountain.operator.caches;


import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.Watchable;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.crds.*;
import org.freemountain.operator.events.DataStoreAccessClaimLifecycleEvent;
import org.freemountain.operator.events.LifecycleEvent;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DataStoreAccessClaimCacheEmitter extends ResourceCacheEmitterInformer<DataStoreAccessClaimResource> {
    @Override
    protected SharedIndexInformer<DataStoreAccessClaimResource> createInformer() {
        return informerFactory().sharedIndexInformerForCustomResource(CRD.DataStoreAccessClaim.CONTEXT,DataStoreAccessClaimResource.class, DataStoreAccessClaimResourceList.class, 60 *1000);
    }

    @Override
    protected ResourceEventHandler<DataStoreAccessClaimResource> createHandler(UnicastProcessor<LifecycleEvent<DataStoreAccessClaimResource>> buffer) {
        return new ResourceEventHandler<>() {
            @Override
            public void onAdd(DataStoreAccessClaimResource obj) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.ADDED, obj));
            }

            @Override
            public void onUpdate(DataStoreAccessClaimResource oldObj, DataStoreAccessClaimResource newObj) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.MODIFIED, newObj));
            }

            @Override
            public void onDelete(DataStoreAccessClaimResource obj, boolean deletedFinalStateUnknown) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.DELETED, obj));
            }
        };
    }

    @Outgoing(DataStoreAccessClaimLifecycleEvent.ADDRESS)
    Publisher<DataStoreAccessClaimLifecycleEvent> connect() {
        return watch().onItem().transform(DataStoreAccessClaimLifecycleEvent::new);
    }
}
