package org.freemountain.operator.templates;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.freemountain.operator.dtos.DataStoreProviderConfig;
import org.freemountain.operator.crds.DataStoreResource;

import java.util.*;

public class DataStoreJobTemplate extends JobTemplate {
    public enum Type {
        CREATE_USER,
        CREATE_DATASTORE;


        @Override
        public String toString() {
            if (super.equals(CREATE_USER)) {
                return "create-user";
            }
            if (super.equals(CREATE_DATASTORE)) {
                return "create-datastore";
            }
            throw new IllegalStateException("Unknown enum state: " + super.toString());
        }
    }

    private DataStoreProviderConfig dataStoreProviderConfig;
    private DataStoreResource dataStoreResource;
    private Type type;
    private final String nameSuffix = String.valueOf(new Date().getTime());

    public static DataStoreJobTemplate createDataStore(DataStoreProviderConfig dataStoreProviderConfig, DataStoreResource dataStoreResource) {
        DataStoreJobTemplate template = new DataStoreJobTemplate();
        template.dataStoreProviderConfig = dataStoreProviderConfig;
        template.dataStoreResource = dataStoreResource;
        template.type = Type.CREATE_DATASTORE;

        return template;
    }

    public String getDataStoreName() {
        return dataStoreResource.getSpec().getName();
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
                .add(type.toString())
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

        List<String> command = new LinkedList<>();
        if (type.equals(Type.CREATE_DATASTORE)) {
            Collections.addAll(command, dataStoreProviderConfig.getJob().getEntry(), "createDatabase", getDataStoreName());
        }

        if (command.size() == 0) {
            throw new IllegalStateException("Not implemented");
        }

        container.setCommand(command);

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
