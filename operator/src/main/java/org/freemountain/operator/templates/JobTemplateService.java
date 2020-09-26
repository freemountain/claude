package org.freemountain.operator.templates;
import org.freemountain.operator.providers.DataStoreConfigProvider;
import org.freemountain.operator.crds.DataStoreResource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class JobTemplateService {
    @Inject
    DataStoreConfigProvider configProvider;

    public Optional<DataStoreJobTemplate> buildCreateDataStoreTemplate(DataStoreResource resource) {
               return  configProvider
                        .getConfig(resource.getSpec().getProvider())
                        .map(config -> DataStoreJobTemplate.createDataStore(config, resource));
    }
}
