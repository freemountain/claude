package org.freemountain.operator.kubernetes.jobs;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobService {
    private static final Logger LOGGER = Logger.getLogger(JobService.class);

    private final KubernetesClient kubernetesClient;
    private final JobEventListener jobListener;

    JobService(ManagedExecutor managedExecutor, KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        this.jobListener  = new JobEventListener(managedExecutor, LOGGER);
    }

    @Incoming(JobLifecycleEvent.ADDRESS)
    public void onJobEvent(JobLifecycleEvent event) {
        jobListener.onJobEvent(event);
    }

    public void create(Job job, JobWatcher watcher) {
        if (watcher != null) {
            this.jobListener.register(job.getMetadata().getName(), watcher);
        }
        kubernetesClient.batch().jobs().create(job);
    }
}
