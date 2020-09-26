package org.freemountain.operator.events;

import io.fabric8.kubernetes.api.model.batch.Job;
import org.freemountain.operator.common.LifecycleType;

import java.util.List;

public class JobLifecycleEvent extends LifecycleEvent<Job> {
    public static final String ADDRESS = "lifecycle.job";

    public JobLifecycleEvent(LifecycleType type, List<Job> resources) {
        super(type, resources);
    }
}
