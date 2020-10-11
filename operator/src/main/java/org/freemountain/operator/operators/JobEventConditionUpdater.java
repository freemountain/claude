package org.freemountain.operator.operators;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import org.freemountain.operator.caches.CachedEmitter;
import org.freemountain.operator.common.*;
import org.freemountain.operator.dtos.BaseCondition;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.jboss.logging.Logger;

public class JobEventConditionUpdater<T extends HasMetadata & HasBaseStatus> {
    private static final Logger LOGGER = Logger.getLogger(JobEventConditionUpdater.class);

    private final CachedEmitter<T> cache;
    private final CRDApiClient<T, ?, ?, ?> client;

    public JobEventConditionUpdater(CachedEmitter<T> cache, CRDApiClient<T, ?, ?, ?> client) {
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
}
