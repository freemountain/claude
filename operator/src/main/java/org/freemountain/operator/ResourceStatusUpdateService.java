package org.freemountain.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@ApplicationScoped
public class ResourceStatusUpdateService {
    @Inject
    @Named("rawDataStoreClient")
    RawCustomResourceOperationsImpl rawDataStoreClient;

    @Inject
    @Named("namespace")
    String namespace;

    @Inject
    ObjectMapper objectMapper;

    public void updateStatus(HasMetadata target, Object Status) {
        try {
            String statusJson = objectMapper.writeValueAsString(target);
            rawDataStoreClient.updateStatus(namespace,  target.getMetadata().getName(), statusJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
