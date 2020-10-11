package org.freemountain.operator.templates;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.batch.Job;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.Constants;
import org.freemountain.operator.common.InstanceConfig;
import org.freemountain.operator.crds.DataStoreAccessClaimResource;
import org.freemountain.operator.dtos.DataStoreUser;
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
                        .map(dataStoreProviderConfig -> new DataStoreJobTemplate.CreateDataStore(instanceConfig, dataStoreProviderConfig, resource));
    }

    public Optional<DataStoreJobTemplate> buildCreateUserTemplate(DataStoreResource resource, DataStoreAccessClaimResource dataStoreAccessClaimResource, DataStoreUser user) {
        return  configProvider
                .getConfig(resource.getSpec().getProvider())
                .map(dataStoreProviderConfig -> new DataStoreJobTemplate.CreateUser(instanceConfig, dataStoreProviderConfig, resource, dataStoreAccessClaimResource, user));
    }

}
