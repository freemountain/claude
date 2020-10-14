package org.freemountain.operator.operators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.batch.Job;
import java.util.Optional;
import java.util.function.Consumer;
import org.freemountain.operator.caches.CachedEmitter;
import org.freemountain.operator.common.*;
import org.freemountain.operator.dtos.BaseCondition;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.jboss.logging.Logger;

public class JobEventConditionUpdater<T extends HasMetadata & HasBaseStatus> {
    private static final Logger LOGGER = Logger.getLogger(JobEventConditionUpdater.class);

    private final ObjectMapper mapper;
    private final CachedEmitter<T> cache;
    private final CRDApiClient<T, ?, ?, ?> client;
    private final Consumer<T> reconcile;

    public JobEventConditionUpdater(
            ObjectMapper mapper,
            CachedEmitter<T> cache,
            CRDApiClient<T, ?, ?, ?> client,
            Consumer<T> reconcile) {
        this.mapper = mapper;
        this.cache = cache;
        this.client = client;
        this.reconcile = reconcile == null ? t -> {} : reconcile;
    }

    public void onJobEvent(JobLifecycleEvent event) {
        var crd = client.crd();

        var job = event.getResource();
        T owner =
                Optional.ofNullable(event.getResource())
                        .flatMap(resource -> ResourceUtils.getOwningCRD(event.getResource(), crd))
                        .map(OwnerReference::getUid)
                        .flatMap(cache::get)
                        .orElse(null);

        if (owner == null || job == null) {
            return;
        }

        var currentOwnerHash = ResourceHash.hash(mapper, owner);
        var jobOwnerHash = parseJobOwnerHash(job).orElse(null);
        boolean specIsTheSame =
                jobOwnerHash != null && currentOwnerHash.getSpec() == jobOwnerHash.getSpec();

        if (event.getType().equals(LifecycleType.DELETED) && !specIsTheSame) {
            reconcile.accept(owner);
        }

        var finishedState = getFinishedState(event).orElse(null);
        if (finishedState == null) {
            return;
        }

        LOGGER.debugf(
                "Job '%s' for %s '%s' changed to %s",
                event.getResource().getMetadata().getName(),
                crd.getConfig().getKind(),
                owner.getMetadata().getName(),
                finishedState);
        updateReadyCondition(owner, finishedState);
    }

    void updateReadyCondition(T owner, JobState state) {
        BaseCondition condition = new BaseCondition();
        condition.setType(ConditionUtils.READY_CONDITION_NAME);
        condition.setStatus(
                state.equals(JobState.SUCCEEDED) ? ConditionUtils.TRUE : ConditionUtils.FALSE);

        var conditions = owner.getStatus() != null ? owner.getStatus().getConditions() : null;
        conditions = ConditionUtils.set(conditions, condition);
        client.status().updateConditions(owner, conditions);
    }

    Optional<ResourceHash> parseJobOwnerHash(Job job) {
        return Optional.ofNullable(job.getMetadata())
                .map(ObjectMeta::getAnnotations)
                .map(annotations -> annotations.get(Constants.RESOURCE_HASH_ANNOTATION))
                .map(
                        hashJson -> {
                            try {
                                return mapper.readValue(hashJson, ResourceHash.class);
                            } catch (JsonProcessingException e) {
                                return null;
                            }
                        });
    }

    private Optional<JobState> getFinishedState(JobLifecycleEvent event) {
        if (!event.getType().equals(LifecycleType.MODIFIED)) {
            return Optional.empty();
        }

        JobState jobState = JobState.from(event.getResource());
        JobState previousJobState = JobState.from(event.getPreviousResource());

        if (jobState != null && !jobState.equals(previousJobState)) {
            return jobState.getFinishedState();
        }

        return Optional.empty();
    }
}
