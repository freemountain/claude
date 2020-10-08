package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
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
import org.freemountain.operator.events.DataStoreLifecycleEvent;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.crds.DataStoreResourceDoneable;
import org.freemountain.operator.crds.DataStoreResourceList;
import org.freemountain.operator.events.LifecycleEvent;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class DataStoreCacheEmitter extends ResourceCacheEmitterInformer<DataStoreResource> {

    @Override
    protected SharedIndexInformer<DataStoreResource> createInformer() {
        return informerFactory().sharedIndexInformerForCustomResource(CRD.DataStore.CONTEXT, DataStoreResource.class, DataStoreResourceList.class, 60 *1000);
    }

    @Override
    protected ResourceEventHandler<DataStoreResource> createHandler(UnicastProcessor<LifecycleEvent<DataStoreResource>> buffer) {
        return new ResourceEventHandler<>() {
            @Override
            public void onAdd(DataStoreResource obj) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.ADDED, obj));
            }

            @Override
            public void onUpdate(DataStoreResource oldObj, DataStoreResource newObj) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.MODIFIED, newObj));
            }

            @Override
            public void onDelete(DataStoreResource obj, boolean deletedFinalStateUnknown) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.DELETED, obj));
            }
        };
    }

    @Outgoing(DataStoreLifecycleEvent.ADDRESS)
    Publisher<DataStoreLifecycleEvent> connect() {
        return watch().onItem().transform(DataStoreLifecycleEvent::new);
    }
}
