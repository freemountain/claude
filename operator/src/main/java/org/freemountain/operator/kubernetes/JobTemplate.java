package org.freemountain.operator.kubernetes;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import org.freemountain.operator.datastore.DataStoreConfig;

import java.util.*;

public class JobTemplate {
    private DataStoreConfig config;

    public JobTemplate(DataStoreConfig config) {
        this.config = config;
    }

    public Job createDatabase(String dbName) {
        String name = config.getName().concat("-").concat("create-database-").concat(dbName).concat(String.valueOf(new Date().getTime()));

        return new JobBuilder()
                .withApiVersion("batch/v1")
                .withNewMetadata()
                .withName(name)
                .endMetadata()
                .withNewSpec()
                .withBackoffLimit(4)
                .withNewTemplate()
                .withNewSpec()
                .addNewContainer()
                .withName("job")
                .withCommand(config.getJob().getEntry(), "createDatabase", dbName)
                .withImage(config.getJob().getImage())
                .addAllToEnv(createEnvironmentVariables())
               // .withArgs("perl", "-Mbignum=bpi", "-wle", "print bpi(2000)")
                .endContainer()
                .withRestartPolicy("Never")
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
    }

    private Collection<EnvVar> createEnvironmentVariables() {
        Collection<EnvVar> env = new LinkedList<>();

        env.add(new EnvVarBuilder().withName("DB_HOST").withValue(config.getHost()).build());
        env.add(new EnvVarBuilder().withName("DB_PORT").withValue(config.getPort()).build());
        env.add(new EnvVarBuilder().withName("DB_ROOT_USER").withValue(config.getUsername()).build());
        env.add(new EnvVarBuilder().withName("DB_ROOT_PASSWORD").withValue(config.getPassword()).build());

        return env;
/*
        EnvVar host = new EnvVar();
        host.setName("DB_HOST");
        host.setValue(config.getHost());
        result.add(host);

        EnvVar port = new EnvVar();
        host.setName("DB_PORT");
        host.setValue(config.getPort());
        result.add(port);

        EnvVar user = new EnvVar();
        host.setName("DB_ROOT_USER");
        host.setValue(config.getUsername());
        result.add(user);

        EnvVar password = new EnvVar();
        host.setName("DB_ROOT_PASSWORD");
        host.setValue(config.getPassword());
        result.add(password);
*/
    }

}
