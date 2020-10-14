package org.freemountain.operator.events;

import io.fabric8.kubernetes.api.model.batch.Job;

public class JobLifecycleEvent extends LifecycleEvent<Job> {
    public static final String ADDRESS = "lifecycle.job";

    public JobLifecycleEvent(LifecycleEvent<Job> other) {
        super(other);
    }
}
