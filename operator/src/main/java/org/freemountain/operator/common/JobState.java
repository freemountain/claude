package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobStatus;

import java.util.Optional;

public enum JobState {
    ACTIVE,
    SUCCEEDED,
    FAILED;

    public Optional<JobState> getFinishedState() {
        switch (this) {
            case ACTIVE: return Optional.of(ACTIVE);
            case FAILED: return Optional.of(FAILED);
            default: return Optional.empty();
        }
    }

    public static JobState from(Job job) {
        if(job == null) {
            return null;
        }

        JobStatus status = job.getStatus();
        Integer active = status.getActive();
        Integer succeeded = status.getSucceeded();
        Integer failed = status.getFailed();

        if(active != null && succeeded == null && failed == null ) {
            return ACTIVE;
        }
        if(active == null && succeeded != null && failed == null ) {
            return SUCCEEDED;
        }
        if(active == null && succeeded == null && failed != null ) {
            return FAILED;
        }

        return null;
    }
}
