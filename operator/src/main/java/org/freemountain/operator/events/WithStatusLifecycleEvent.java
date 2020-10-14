package org.freemountain.operator.events;

import org.freemountain.operator.common.ConditionUtils;
import org.freemountain.operator.common.HasBaseStatus;
import org.freemountain.operator.common.LifecycleType;

public class WithStatusLifecycleEvent<T extends HasBaseStatus> extends LifecycleEvent<T> {
    public WithStatusLifecycleEvent(LifecycleEvent<T> other) {
        super(other);
    }

    public boolean shouldCreateJob() {
        if (this.getType().equals(LifecycleType.DELETED)) {
            return false;
        }
        if (this.getType().equals(LifecycleType.ADDED)) {
            return true;
        }

        boolean shouldCreateJob = shouldCreateJob(this.getResource());
        boolean previousShouldCreateJob = shouldCreateJob(this.getPreviousResource());

        return shouldCreateJob && !previousShouldCreateJob;
    }

    private static boolean shouldCreateJob(HasBaseStatus resource) {
        if (resource.getStatus() == null || resource.getStatus().getConditions() == null) {
            return true;
        }
        return ConditionUtils.isTrue(
                resource.getStatus().getConditions(), ConditionUtils.READY_CONDITION_NAME);
    }
}
