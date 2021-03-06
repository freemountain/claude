package org.freemountain.operator.kubernetes;

import io.fabric8.kubernetes.client.Watcher;
import io.quarkus.runtime.StartupEvent;
import org.freemountain.operator.MySqlClient;
import org.freemountain.operator.MySqlQueryGenerator;
import org.freemountain.operator.kubernetes.caches.DataStoreResourceCache;
import org.freemountain.operator.kubernetes.resources.DataStoreResource;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.sql.SQLException;

public class DataStoreOperator {
    private static final Logger LOGGER = Logger.getLogger(DataStoreOperator.class);

    MySqlQueryGenerator queryGenerator = new MySqlQueryGenerator();

    @Inject
    private DataStoreResourceCache cache;

    @Inject
    private MySqlClient dbClient;

    void runWatch() {
        new Thread(() -> cache.listThenWatch(this::handleEvent)).start();
    }

    private void handleEvent(Watcher.Action action, String uid) {
        try {
            DataStoreResource resource = cache.get(uid);
            if (resource == null) {
                return;
            }
            onDatabase(action, resource);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void onDatabase(Watcher.Action action, DataStoreResource db) throws SQLException {
        String dbName = db.getMetadata().getName();
        LOGGER.infof("%s %s %s", action, dbName, db.getSpec());

        dbClient.execute(queryGenerator.createDatabaseIfNotExists(dbName));
        /*
        Map<String, DatabaseUser.Role> roles = collectRoles(db.getSpec());
        Map<String, Optional<Secret>> allSecrets = collectSecrets(db.getSpec());

        List<String> unknownSecrets = allSecrets.entrySet().stream().filter(e -> e.getValue().isEmpty()).map(Map.Entry::getKey).collect(Collectors.toList());
        if(unknownSecrets.size() > 0) {
            LOGGER.infof("Unknown secrets: %s", unknownSecrets);
        }
        List<Secret> secrets = allSecrets.values().stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        List<DatabaseUser> usersToCreate = createDatabaseUsers(roles, secrets);
        for(DatabaseUser user: usersToCreate) {
            LOGGER.infof("Create %s", user);
        }

        dbClient.createDatabase(dbName, usersToCreate);
        */

    }
/*
    private Map<String, DatabaseUser.Role> collectRoles(DataStoreSpec spec) {
        Map<String, DatabaseUser.Role> roles = new HashMap<>();

        if(spec.getReadSelector() != null) {
            for (String name : spec.getReadSelector()) {
                roles.put(name, DatabaseUser.Role.READ);
            }
        }

        if(spec.getWriteSelector() != null) {
            for (String name : spec.getWriteSelector()) {
                roles.compute(name, (k, currentRole) -> currentRole == null ? DatabaseUser.Role.WRITE : DatabaseUser.Role.BOTH);
            }
        }

        return roles;
    }

    private Map<String, Optional<Secret>> collectSecrets(DataStoreSpec spec) {
        Set<String> names = new HashSet<>();
        if(spec.getWriteSelector() != null) {
            names.addAll(spec.getWriteSelector());
        }
        if(spec.getReadSelector() != null) {
            names.addAll(spec.getReadSelector());
        }
        Map<String, Optional<Secret>> result = new HashMap<>();
        for(String name : names) {
            try {
                result.put(name, Optional.empty());
                Resource<Secret, DoneableSecret> secretRef = client.secrets().withName(name);
                if(secretRef == null) {
                    continue;
                }
                Secret secret = secretRef.get();
                result.put(name, Optional.ofNullable(secret));
            } catch (Exception e) {
                LOGGER.errorf(e,"Getting secret %s failed", name);
            }
        }

        return result;
    }

    private List<DatabaseUser> createDatabaseUsers(Map<String, DatabaseUser.Role> roles, List<Secret> secrets) {
        List<DatabaseUser> users = new LinkedList<>();
        for(Secret secret : secrets) {
            String key = secret.getMetadata().getName();
            DatabaseUser.Role role = roles.get(key);
            String username = getDecodedSecretValue(secret, "username");
            String password = getDecodedSecretValue(secret, "password");
            users.add(new DatabaseUser(username, password, role));
        }
        return users;
    }

    private String getDecodedSecretValue(Secret secret, String key) {
        Map<String, String> data = secret.getData();
        String value = data.get(key);
        return value == null ? null :  new String(Base64.getDecoder().decode(value));
    }

*/
}
