package org.freemountain.operator.templates;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.Constants;
import org.freemountain.operator.common.InstanceConfig;
import org.freemountain.operator.providers.DataStoreConfigProvider;
import org.freemountain.operator.crds.DataStoreResource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class JobTemplateService {
    @Inject
    DataStoreConfigProvider configProvider;

    @Inject
    InstanceConfig instanceConfig;

    public Optional<DataStoreJobTemplate> buildCreateDataStoreTemplate(DataStoreResource resource) {
               return  configProvider
                        .getConfig(resource.getSpec().getProvider())
                        .map(dataStoreProviderConfig -> DataStoreJobTemplate.createDataStore(instanceConfig, dataStoreProviderConfig, resource));
    }

    public Optional<OwnerReference> getOwningResource(CRD.Type type, HasMetadata resource) {
        Map<String, String> labels = resource.getMetadata().getLabels();
        labels = labels == null ? Collections.emptyMap() : labels;

        if(!instanceConfig.getId().equals(labels.get(Constants.INSTANCE_ID_LABEL))) {
            return Optional.empty();
        }

        List<OwnerReference> owners = resource.getMetadata().getOwnerReferences();
        owners = owners == null ? Collections.emptyList() : owners;

        return owners.stream()
                .filter(owner -> type.equals(CRD.Type.from(owner).orElse(null)))
                .findFirst();
    }
}
