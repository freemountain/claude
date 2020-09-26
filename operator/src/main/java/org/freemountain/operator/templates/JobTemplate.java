package org.freemountain.operator.templates;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;

import java.util.*;

public abstract class JobTemplate {

    public abstract String getName();
    public abstract Collection<Container> getContainers();

    public int getBackoffLimit() {
        return 4;
    };

    public String getRestartPolicy() {
        return "Never";
    };

    public Optional<HasMetadata > getOwner() {
        return Optional.empty();
    }

    public List<OwnerReference>  getOwnerReferences() {
        return getOwner()
                .map(owner -> {
                    OwnerReference result = new OwnerReference();
                    result.setKind(owner.getKind());
                    result.setApiVersion(owner.getApiVersion());
                    result.setUid(owner.getMetadata().getUid());
                    result.setName(owner.getMetadata().getName());
                    result.setController(true);

                    return result;
                })
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    public Job getJob() {
        return new JobBuilder()
                .withApiVersion("batch/v1")
                .withNewMetadata()
                .withName(getName())
                .withOwnerReferences(getOwnerReferences())
                .endMetadata()
                .withNewSpec()
                .withBackoffLimit(getBackoffLimit())
                .withNewTemplate()
                .withNewSpec()
                .addAllToContainers(getContainers())
                .withRestartPolicy(getRestartPolicy())
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
    }

    public boolean isFromThisTemplate(Job job) {
        List<OwnerReference> ownerReferences = job.getMetadata().getOwnerReferences();
        if(ownerReferences.size() != getOwnerReferences().size() || !ownerReferences.containsAll(getOwnerReferences())) {
            return false;
        }
        return getName().equals(job.getMetadata().getName());

    }
}
