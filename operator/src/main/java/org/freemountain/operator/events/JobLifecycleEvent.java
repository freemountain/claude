package org.freemountain.operator.events;

import io.fabric8.kubernetes.api.model.batch.Job;
import org.freemountain.operator.common.JobState;
import org.freemountain.operator.common.LifecycleType;

import java.util.List;

public class JobLifecycleEvent extends LifecycleEvent<Job> {
    public static final String ADDRESS = "lifecycle.job";

    private final JobState jobState;
    private final JobState previousJobState;

    public JobLifecycleEvent(LifecycleEvent<Job> other) {
        super(other);
        jobState = JobState.from(getResource());
        previousJobState = JobState.from(getPreviousResource());
    }

    public JobState getJobState() {
        return jobState;
    }

    public JobState getPreviousJobState() {
        return previousJobState;
    }

    public boolean isFinishedEvent() {
        if (!type.equals(LifecycleType.MODIFIED)) {
            return false;
        }

        return jobState != null && !jobState.equals(previousJobState) && !jobState.equals(JobState.ACTIVE);
    }
}
