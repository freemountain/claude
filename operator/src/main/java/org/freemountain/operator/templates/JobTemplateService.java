package org.freemountain.operator.templates;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Inject
    ObjectMapper mapper;

    public Optional<DataStoreJobTemplate> buildCreateDataStoreTemplate(DataStoreResource resource) {
               return  configProvider
                        .getConfig(resource.getSpec().getProvider())
                        .map(dataStoreProviderConfig -> new DataStoreJobTemplate.CreateDataStore(dataStoreProviderConfig, resource))
                        .map(this::inject)
                       ;
    }

    public Optional<DataStoreJobTemplate> buildCreateUserTemplate(DataStoreResource resource, DataStoreAccessClaimResource dataStoreAccessClaimResource, DataStoreUser user) {
        return  configProvider
                .getConfig(resource.getSpec().getProvider())
                .map(dataStoreProviderConfig -> new DataStoreJobTemplate.CreateUser(dataStoreProviderConfig, resource, dataStoreAccessClaimResource, user))
                .map(this::inject);
    }

    DataStoreJobTemplate inject(DataStoreJobTemplate template) {
        template.mapper = mapper;
        template.instanceConfig = instanceConfig;

        return template;
    }
}
