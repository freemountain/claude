package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.batch.DoneableJob;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.*;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.crds.DataStoreResourceList;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.freemountain.operator.events.LifecycleEvent;
import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

public class JobCacheEmitter extends CachedEmitter<Job> {
    static class JobHandler extends LifecycleClient.KubernetesEventHandler<Job> implements ResourceEventHandler<Job> {};

    @Outgoing(JobLifecycleEvent.ADDRESS)
    @Broadcast
    Publisher<JobLifecycleEvent> connect() {
        SharedIndexInformer<Job> informer = getInformerFactory().sharedIndexInformerFor(Job.class, JobList.class, 60 *1000);
        LifecycleClient<Job> client = new LifecycleClient<Job>(informer, new JobHandler());
        return super.connect(client).onItem().transform(JobLifecycleEvent::new);
    }
}
