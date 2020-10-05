package org.freemountain.operator.events;

import io.fabric8.kubernetes.api.model.batch.Job;
import org.freemountain.operator.common.JobState;
import org.freemountain.operator.common.LifecycleType;

import java.util.List;

public class JobLifecycleEvent extends LifecycleEvent<Job> {
    public static final String ADDRESS = "lifecycle.job";

    private final JobState jobState;

    public JobLifecycleEvent(LifecycleEvent<Job> other) {
        super(other);
        jobState = JobState.from(getResource());
    }

    public JobState getJobState() {
        return jobState;
    }
}
