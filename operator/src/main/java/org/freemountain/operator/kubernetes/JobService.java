package org.freemountain.operator.kubernetes;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Createable;
import org.freemountain.operator.DataStoreConfigProvider;
import org.freemountain.operator.datastore.DataStoreConfig;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class JobService {
    private static final Logger LOGGER = Logger.getLogger(JobService.class);

    @Inject
    DataStoreConfigProvider configProvider;

    @Inject
    KubernetesClient kubernetesClient;


    void startJob(String dataStoreName, String databaseName) {
        DataStoreConfig config = configProvider.getConfig(dataStoreName);
        JobTemplate template = new JobTemplate(config);
        Job job = template.createDatabase(databaseName);

        Job createable = kubernetesClient.batch().jobs().create(job);
        LOGGER.infof("added job %s", createable);

    }

}
