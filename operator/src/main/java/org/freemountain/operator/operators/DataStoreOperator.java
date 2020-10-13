package org.freemountain.operator.operators;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.freemountain.operator.caches.DataStoreCacheEmitter;
import org.freemountain.operator.crds.DataStoreApiClient;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.events.DataStoreLifecycleEvent;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.freemountain.operator.templates.DataStoreJobTemplate;
import org.freemountain.operator.templates.JobTemplateService;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class DataStoreOperator {
    private static final Logger LOGGER = Logger.getLogger(DataStoreOperator.class);

    @Inject
    DataStoreCacheEmitter dataStoreCache;

    @Inject
    JobTemplateService jobTemplateService;

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    DataStoreApiClient dataStoreClient;

    @Inject
    ObjectMapper mapper;

    JobEventConditionUpdater<DataStoreResource> jobCondition;

    @PostConstruct
    public void init() {
        jobCondition = new JobEventConditionUpdater<>(mapper, dataStoreCache, dataStoreClient, null);
    }

    @Incoming(JobLifecycleEvent.ADDRESS)
    void onJobEvent(JobLifecycleEvent event) {
        jobCondition.onJobEvent(event);
    }

    @Incoming(DataStoreLifecycleEvent.ADDRESS)
    void onDataStoreEvent(DataStoreLifecycleEvent event) {
        DataStoreResource resource = event.getResource();
        if (!event.shouldCreateJob()) {
            return;
        }

        Optional<DataStoreJobTemplate> jobTemplate = jobTemplateService.buildCreateDataStoreTemplate(resource);
        if (jobTemplate.isEmpty()) {
            LOGGER.error("Provider '%s' is not configured");
            System.exit(-1);
            return;
        }

        LOGGER.debugf("Spawned create job '%s' for datasource '%s'", jobTemplate.get().getJob().getMetadata().getName(), resource.getMetadata().getName());
        kubernetesClient.batch().jobs().create(jobTemplate.get().getJob());
    }

}
