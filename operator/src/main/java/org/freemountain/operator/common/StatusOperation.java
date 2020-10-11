package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.freemountain.operator.dtos.BaseCondition;
import org.freemountain.operator.dtos.BaseStatus;
import org.jboss.logging.Logger;

import java.util.List;

public class StatusOperation<T extends HasMetadata & HasBaseStatus> {
    private static final Logger LOGGER = Logger.getLogger(StatusOperation.class);

    MixedOperation<T, ?, ?, ?> typedClient;
    CRDContext<T> crd;

    public StatusOperation(MixedOperation<T, ?, ?, ?> typedClient, CRDContext<T> crd) {
        this.typedClient = typedClient;
        this.crd = crd;
    }

    public T updateConditions(T target, List<BaseCondition> conditions) {
        var cloned = crd.clone(target);
        var status = cloned.getStatus() == null ? new BaseStatus() : cloned.getStatus();
        status.setConditions(conditions);
        cloned.setStatus(status);

        try {
            return typedClient.updateStatus(cloned);
        } catch (KubernetesClientException e) {
            LOGGER.warnf("updateStatus failed with '%s'", e.getMessage());
            return null;
        }


    }
}
