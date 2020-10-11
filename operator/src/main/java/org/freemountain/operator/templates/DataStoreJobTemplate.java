package org.freemountain.operator.templates;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.freemountain.operator.common.Constants;
import org.freemountain.operator.common.InstanceConfig;
import org.freemountain.operator.crds.DataStoreAccessClaimResource;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.dtos.DataStoreProviderConfig;
import org.freemountain.operator.dtos.DataStoreUser;

import java.util.*;

public abstract class DataStoreJobTemplate extends JobTemplate {
    public static class CreateDataStore extends DataStoreJobTemplate {
        public CreateDataStore(InstanceConfig instanceConfig, DataStoreProviderConfig dataStoreProviderConfig, DataStoreResource dataStoreResource) {
            super(instanceConfig, dataStoreProviderConfig, dataStoreResource);
            this.typeName = "create-datastore";
        }

        @Override
        public List<String> getCommand() {
            List<String> command = new LinkedList<>();
            Collections.addAll(command, dataStoreProviderConfig.getJob().getEntry(), "createDatabase", getDataStoreName());
            return command;
        }
    }

    public static class CreateUser extends DataStoreJobTemplate {
        DataStoreUser user;
        DataStoreAccessClaimResource dataStoreAccessClaimResource;

        public CreateUser(InstanceConfig instanceConfig, DataStoreProviderConfig dataStoreProviderConfig, DataStoreResource dataStoreResource, DataStoreAccessClaimResource dataStoreAccessClaimResource, DataStoreUser user) {
            super(instanceConfig, dataStoreProviderConfig, dataStoreResource);
            this.dataStoreAccessClaimResource = dataStoreAccessClaimResource;
            this.user= user;
            this.typeName = "create-user";
        }

        @Override
        public List<String> getCommand() {
            List<String> command = new LinkedList<>();
            Collections.addAll(command, dataStoreProviderConfig.getJob().getEntry(), "createUserWithPrivileges", getDataStoreName(), user.getUsername(), user.getPassword());
            command.addAll(user.getRoles());
            return command;
        }

        @Override
        public Optional<HasMetadata> getOwner() {
            return Optional.of(dataStoreAccessClaimResource);
        }
    }


    protected InstanceConfig instanceConfig;
    protected DataStoreProviderConfig dataStoreProviderConfig;
    protected DataStoreResource dataStoreResource;
    protected String typeName = "";
    protected final String nameSuffix = String.valueOf(new Date().getTime());

    abstract public List<String> getCommand();


    public DataStoreJobTemplate(InstanceConfig instanceConfig, DataStoreProviderConfig dataStoreProviderConfig, DataStoreResource dataStoreResource) {
        this.instanceConfig = instanceConfig;
        this.dataStoreProviderConfig = dataStoreProviderConfig;
        this.dataStoreResource = dataStoreResource;
    }

    public String getDataStoreName() {
        return dataStoreResource.getSpec().getName();
    }


    @Override
    public Map<String, String> getLabels() {
        Map<String, String> labels = super.getLabels();

        labels.put(Constants.INSTANCE_ID_LABEL, instanceConfig.getId());

        return labels;
    }

    @Override
    public Optional<HasMetadata> getOwner() {
        return Optional.of(dataStoreResource);
    }

    @Override
    public String getName() {
        StringJoiner nameParts = new StringJoiner("-");
        return nameParts
                .add(dataStoreProviderConfig.getName())
                .add(typeName)
                .add(getDataStoreName())
                .add(nameSuffix)
                .toString();
    }

    @Override
    public Collection<Container> getContainers() {
        Container container = new Container();

        container.setEnv(getContainerEnvironmentVariables());
        container.setImage(dataStoreProviderConfig.getJob().getImage());
        container.setName(getName());
        container.setCommand(getCommand());

        return Collections.singleton(container);
    }


    public List<EnvVar> getContainerEnvironmentVariables() {
        List<EnvVar> env = new LinkedList<>();

        env.add(new EnvVarBuilder().withName("DB_HOST").withValue(dataStoreProviderConfig.getHost()).build());
        env.add(new EnvVarBuilder().withName("DB_PORT").withValue(dataStoreProviderConfig.getPort()).build());
        env.add(new EnvVarBuilder().withName("DB_ROOT_USER").withValue(dataStoreProviderConfig.getUsername()).build());
        env.add(new EnvVarBuilder().withName("DB_ROOT_PASSWORD").withValue(dataStoreProviderConfig.getPassword()).build());

        return env;
    }
}
