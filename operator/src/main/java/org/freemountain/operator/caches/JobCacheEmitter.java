package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.batch.DoneableJob;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.*;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
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

public class JobCacheEmitter extends ResourceCacheEmitterInformer<Job> {
    private static final Logger LOGGER = Logger.getLogger(JobCacheEmitter.class);

    @Override
    protected SharedIndexInformer<Job> createInformer() {
        return informerFactory().sharedIndexInformerFor(Job.class, JobList.class, 60 *1000);
    }

    @Override
    protected ResourceEventHandler<Job> createHandler(UnicastProcessor<LifecycleEvent<Job>> buffer) {
        return new ResourceEventHandler<>() {
            @Override
            public void onAdd(Job obj) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.ADDED, obj));
            }

            @Override
            public void onUpdate(Job oldObj, Job newObj) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.MODIFIED, newObj));
            }

            @Override
            public void onDelete(Job obj, boolean deletedFinalStateUnknown) {
                buffer.onNext(new LifecycleEvent<>(LifecycleType.DELETED, obj));
            }
        };
    }

    @Outgoing(JobLifecycleEvent.ADDRESS)
    @Broadcast
    Publisher<JobLifecycleEvent> connect() {
        return watch().onItem().transform(JobLifecycleEvent::new);
    }
}
