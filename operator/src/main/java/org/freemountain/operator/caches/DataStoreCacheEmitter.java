package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
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
import org.freemountain.operator.common.CRDContext;
import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.events.DataStoreLifecycleEvent;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.crds.DataStoreResourceDoneable;
import org.freemountain.operator.crds.DataStoreResourceList;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.freemountain.operator.events.LifecycleEvent;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class DataStoreCacheEmitter extends CachedEmitter<DataStoreResource> {
    static class DataStoreHandler extends LifecycleClient.KubernetesEventHandler<DataStoreResource> implements ResourceEventHandler<DataStoreResource> {};

    @Inject
    CRDContext<DataStoreResource> ctx;

    @Outgoing(DataStoreLifecycleEvent.ADDRESS)
    @Broadcast
    Publisher<DataStoreLifecycleEvent> connect() {
        SharedIndexInformer<DataStoreResource> informer = getInformerFactory().sharedIndexInformerForCustomResource(ctx.getConfig(), DataStoreResource.class, DataStoreResourceList.class, 60 *1000);
        LifecycleClient<DataStoreResource> client = new LifecycleClient<DataStoreResource>(informer, new DataStoreHandler());
        return super.connect(client).onItem().transform(DataStoreLifecycleEvent::new);
    }

}
