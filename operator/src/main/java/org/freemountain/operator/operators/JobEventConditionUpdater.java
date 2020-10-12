package org.freemountain.operator.operators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.batch.Job;
import org.freemountain.operator.caches.CachedEmitter;
import org.freemountain.operator.common.*;
import org.freemountain.operator.dtos.BaseCondition;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.jboss.logging.Logger;

import java.util.Optional;

public class JobEventConditionUpdater<T extends HasMetadata & HasBaseStatus> {
    private static final Logger LOGGER = Logger.getLogger(JobEventConditionUpdater.class);

    private final ObjectMapper mapper;
    private final CachedEmitter<T> cache;
    private final CRDApiClient<T, ?, ?, ?> client;

    public JobEventConditionUpdater(ObjectMapper mapper, CachedEmitter<T> cache, CRDApiClient<T, ?, ?, ?> client) {
        this.mapper = mapper;
        this.cache = cache;
        this.client = client;
    }

    public void onJobEvent(JobLifecycleEvent event) {
        if (!event.isFinishedEvent()) {
            return;
        }

        var crd = client.crd();
        T owner = ResourceUtils.getOwningCRD(event.getResource(), crd)
                .map(OwnerReference::getUid)
                .flatMap(cache::get)
                .orElse(null);

        if (owner == null) {
            return;
        }


        LOGGER.debugf("Job '%s' for %s '%s' changed to %s", event.getResource().getMetadata().getName(), crd.getConfig().getKind(), owner.getMetadata().getName(), event.getJobState());

        var ownerHash = parseOwnerHash(event.getResource()).orElse(null);
        var currentHash = ResourceHash.hash(mapper, owner);
        if(ownerHash != null && ownerHash.getSpec() != currentHash.getSpec()) {
            LOGGER.debugf("Spec of %s '%s' changed", crd.getConfig().getKind(), owner.getMetadata().getName());
            return;
        }

        BaseCondition condition = new BaseCondition();
        condition.setType(ConditionUtils.READY_CONDITION_NAME);
        condition.setStatus(ConditionUtils.TRUE);


        if (event.getJobState().equals(JobState.FAILED)) {
            condition.setStatus(ConditionUtils.FALSE);
        }

        var conditions = owner.getStatus() != null ? owner.getStatus().getConditions() : null;
        conditions = ConditionUtils.set(conditions, condition);
        client.status().updateConditions(owner, conditions);
    }

    Optional<ResourceHash> parseOwnerHash(Job job) {
        return Optional.ofNullable(job.getMetadata())
                .map(ObjectMeta::getAnnotations)
                .map(annotations -> annotations.get(Constants.RESOURCE_HASH_ANNOTATION))
                .map(hashJson -> {
                    try {
                        return mapper.readValue(hashJson, ResourceHash.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                });
    }
}
