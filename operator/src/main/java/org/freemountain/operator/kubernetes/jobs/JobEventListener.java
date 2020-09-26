package org.freemountain.operator.kubernetes.jobs;

import io.fabric8.kubernetes.api.model.batch.Job;
import org.freemountain.operator.common.JobState;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.freemountain.operator.common.LifecycleType;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JobEventListener {
    private static class Entry {
        Set<JobWatcher> watchers = new HashSet<>();
        boolean started = false;
    }

    private final Map<String, Entry> map = Collections.synchronizedMap(new HashMap<>());
    private final ExecutorService executorService;
    private final Logger logger;

    public JobEventListener(ExecutorService executorService, Logger logger) {
        this.executorService = executorService;
        this.logger = logger;
    }

    void register(String key, JobWatcher watcher) {
        synchronized (map) {
            Entry entry = map.get(key);
            if (entry == null) {
                entry = new Entry();
                map.put(key, entry);
            }
            entry.watchers.add(watcher);
        }
    }

    void onJobEvent(JobLifecycleEvent event) {
        if (LifecycleType.DELETED.equals(event.getType())) {
            for (Job job : event.getResources()) {
                map.remove(job.getMetadata().getName());
            }
            return;
        }
        for (Job job : event.getResources()) {
            String key = job.getMetadata().getName();
            Optional<JobState> state = JobState.from(job);
            if (state.isEmpty()) {
                continue;
            }

            collectHandler(key, state.get()).forEach(handler -> callHandler(job, handler));
        }
    }

    private void callHandler(Job job, Consumer<Job> handler) {
        CompletableFuture
                .runAsync(() -> handler.accept(job), executorService)
                .exceptionally(e -> {
                    logger.error("handler failed", e);
                    return null;
                });
    }

    private Collection<Consumer<Job>> collectHandler(String key, JobState state) {
        synchronized (map) {
            Entry entry = map.get(key);
            if (entry == null) {
                return Collections.emptyList();
            }

            Function<JobWatcher, Consumer<Job>> mapper = null;

            if (JobState.ACTIVE.equals(state) && !entry.started) {
                entry.started = true;
                mapper = jobWatcher -> (Consumer<Job>) jobWatcher::onActive;
            }

            if (JobState.SUCCEEDED.equals(state) && entry.started) {
                map.remove(key);
                mapper = jobWatcher -> (Consumer<Job>) jobWatcher::onSucceeded;
            }
            if (JobState.FAILED.equals(state) && entry.started) {
                map.remove(key);
                mapper = jobWatcher -> (Consumer<Job>) jobWatcher::onFailed;
            }

            if (mapper == null) {
                return Collections.emptyList();
            }

            return entry.watchers.stream().map(mapper).collect(Collectors.toList());
        }
    }
}
