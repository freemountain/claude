package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobStatus;

import java.util.Optional;

public enum JobState {
    ACTIVE,
    SUCCEEDED,
    FAILED,
    UNKNOWN;

    public static JobState from(Job job) {
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
        return UNKNOWN;
    }
}
