package org.freemountain.operator.operators;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.freemountain.operator.caches.DataStoreCacheEmitter;
import org.freemountain.operator.common.*;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.crds.DataStoreResourceDoneable;
import org.freemountain.operator.crds.DataStoreResourceList;
import org.freemountain.operator.dtos.BaseCondition;
import org.freemountain.operator.events.DataStoreLifecycleEvent;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.freemountain.operator.templates.DataStoreJobTemplate;
import org.freemountain.operator.templates.JobTemplateService;
import org.jboss.logging.Logger;

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
    NonNamespaceOperation<DataStoreResource, DataStoreResourceList, DataStoreResourceDoneable, Resource<DataStoreResource, DataStoreResourceDoneable>> dataStoreClient;

    @Incoming(JobLifecycleEvent.ADDRESS)
    void onJobEvent(JobLifecycleEvent event) {
        if (!event.isFinishedEvent()) {
            return;
        }

        DataStoreResource dataStore = jobTemplateService.getOwningResource(CRD.Type.DATA_STORE, event.getResource())
                .map(OwnerReference::getUid)
                .flatMap(dataStoreCache::get)
                .orElse(null);

        if (dataStore == null) {
            return;
        }

        LOGGER.debugf("Job '%s' for dataSource '%s' changed to %s",event.getResource().getMetadata().getName(), dataStore.getMetadata().getName(), event.getJobState());

        BaseCondition condition = new BaseCondition();
        condition.setType(ConditionUtils.READY_CONDITION_NAME);
        condition.setStatus(ConditionUtils.TRUE);

        if (event.getJobState().equals(JobState.FAILED)) {
            condition.setStatus(ConditionUtils.FALSE);
        }

        var conditions = dataStore.getStatus() != null ? dataStore.getStatus().getConditions() : null;

        ConditionUtils.updateConditions(dataStoreClient::updateStatus, DataStoreResource::new, dataStore, ConditionUtils.set(conditions, condition));
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
