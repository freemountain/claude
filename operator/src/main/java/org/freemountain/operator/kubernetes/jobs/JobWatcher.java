package org.freemountain.operator.kubernetes.jobs;

import io.fabric8.kubernetes.api.model.batch.Job;

public interface JobWatcher {
    void onActive(Job job);

    void onSucceeded(Job job);

    void onFailed(Job job);
}
