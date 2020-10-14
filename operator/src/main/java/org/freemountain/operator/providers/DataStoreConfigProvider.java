package org.freemountain.operator.providers;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;
import org.freemountain.operator.dtos.DataStoreProviderConfig;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DataStoreConfigProvider {
    private static final String CONFIG_PREFIX = "datastore";
    private static final Logger LOG = Logger.getLogger(DataStoreConfigProvider.class);

    private final Map<String, DataStoreProviderConfig> configMap = new HashMap<>();

    public Optional<DataStoreProviderConfig> getConfig(String key) {
        return Optional.ofNullable(configMap.get(key));
    }

    @PostConstruct
    void load() {
        this.configMap.clear();

        for (String key : getKeys()) {
            LOG.debugf("Loading DataStore configuration '%s'", key);
            DataStoreProviderConfig config = getDataStoreConfigValue(key);
            configMap.put(key, config);
        }
    }

    private DataStoreProviderConfig getDataStoreConfigValue(String key) {
        DataStoreProviderConfig config = new DataStoreProviderConfig();

        config.setName(key);
        config.setHost(getConfigStringValue(key, "host"));
        config.setPort(getConfigStringValue(key, "port"));
        config.setUsername(getConfigStringValue(key, "username"));
        config.setPassword(getConfigStringValue(key, "password"));
        config.getJob().setImage(getConfigStringValue(key, "job", "image"));
        config.getJob().setEntry(getConfigStringValue(key, "job", "entry"));

        return config;
    }

    private String getConfigStringValue(String... path) {
        return getConfigValue(String.class, path);
    }

    private <T> T getConfigValue(Class<T> type, String... path) {
        List<String> fullPath = new LinkedList<String>(Collections.singleton(CONFIG_PREFIX));
        fullPath.addAll(Arrays.asList(path));
        String key = String.join(".", fullPath);

        return ConfigProvider.getConfig().<T>getValue(key, type);
    }

    private Set<String> getKeys() {
        return StreamSupport.stream(
                        ConfigProvider.getConfig().getPropertyNames().spliterator(), false)
                .filter(prop -> prop.startsWith(CONFIG_PREFIX))
                .map(prop -> prop.split("\\."))
                .filter(prop -> prop.length >= 2)
                .map(prop -> prop[1])
                .collect(Collectors.toSet());
    }
}
