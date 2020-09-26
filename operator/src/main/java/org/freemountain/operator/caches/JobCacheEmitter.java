package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.batch.DoneableJob;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.*;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

public class JobCacheEmitter extends ResourceCacheEmitter<Job, JobList, DoneableJob, ScalableResource<Job, DoneableJob>> {
    @Inject
    KubernetesClient client;

    @Override
    protected Listable<JobList> getListClient() {
        return client.batch().jobs();
    }

    @Override
    protected Watchable<?, Watcher<Job>> getWatchClient() {
        return client.batch().jobs();
    }

    @Outgoing(JobLifecycleEvent.ADDRESS)
    Publisher<JobLifecycleEvent> connect() {
        return watch().onItem().apply(event -> new JobLifecycleEvent(event.getType(), event.getResources()));
    }


}
