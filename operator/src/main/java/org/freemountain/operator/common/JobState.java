package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobStatus;

import java.util.Optional;

public enum JobState {
    ACTIVE,
    SUCCEEDED,
    FAILED;

    public static Optional<JobState> from(Job job) {
        JobStatus status = job.getStatus();
        Integer active = status.getActive();
        Integer succeeded = status.getSucceeded();
        Integer failed = status.getFailed();

        if(active != null && succeeded == null && failed == null ) {
            return Optional.of(ACTIVE);
        }
        if(active == null && succeeded != null && failed == null ) {
            return Optional.of(SUCCEEDED);
        }
        if(active == null && succeeded == null && failed != null ) {
            return Optional.of(FAILED);
        }
        return Optional.empty();
    }
}
