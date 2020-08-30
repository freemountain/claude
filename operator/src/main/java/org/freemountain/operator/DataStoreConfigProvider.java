package org.freemountain.operator;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.ConfigProvider;
import org.freemountain.operator.datastore.DataStoreConfig;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jboss.logging.Logger;

@ApplicationScoped
public class DataStoreConfigProvider {
    private static String CONFIG_PREFIX = "datastore";
    private static final Logger LOG = Logger.getLogger(DataStoreConfigProvider.class);

    private Map<String, DataStoreConfig> configMap = new HashMap<>();

    public DataStoreConfig getConfig(String key) {
        return configMap.get(key);
    }

    @PostConstruct
    void load() {
        this.configMap.clear();

        for (String key: getKeys()) {
            LOG.debugf("Loading DataStore configuration '%s'", key);
            DataStoreConfig config = getDataStoreConfigValue(key);
            configMap.put(key, config);
        }
    }

    private DataStoreConfig getDataStoreConfigValue(String key) {
        DataStoreConfig config = new DataStoreConfig();

        config.setName(key);
        config.setHost(getConfigStringValue(key, "host"));
        config.setPort(getConfigStringValue(key, "port"));
        config.setUsername(getConfigStringValue(key, "username"));
        config.setPassword(getConfigStringValue(key, "password"));
        config.getJob().setImage(getConfigStringValue(key, "job", "image"));
        config.getJob().setEntry(getConfigStringValue(key, "job", "entry"));

        return config;
    }

    private String getConfigStringValue( String... path) {
        return getConfigValue(String.class, path);
    }

    private <T> T getConfigValue(Class<T> type, String... path) {
        List<String> fullPath =  new LinkedList<String>(Collections.singleton(CONFIG_PREFIX));
        fullPath.addAll(Arrays.asList(path));
        String key = String.join(".", fullPath);

        return ConfigProvider.getConfig().<T>getValue(key, type);
    }

    private Set<String> getKeys() {
       return StreamSupport.stream(ConfigProvider.getConfig().getPropertyNames().spliterator(), false)
                .filter(prop -> prop.startsWith(CONFIG_PREFIX))
                .map(prop -> prop.split("\\."))
                .filter(prop -> prop.length >= 2)
                .map(prop -> prop[1])
                .collect(Collectors.toSet());
    }
}