package org.freemountain.operator.kubernetes;

import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.StartupEvent;
import org.freemountain.operator.MySqlClient;
import org.freemountain.operator.MySqlQueryGenerator;
import org.freemountain.operator.kubernetes.caches.DataStoreAccessClaimResourceCache;
import org.freemountain.operator.kubernetes.resources.DataStoreAccessClaimResource;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DataStoreAccessClaimOperator {
    private static final Logger LOGGER = Logger.getLogger(DataStoreAccessClaimOperator.class);
    @Inject
    protected DataStoreAccessClaimResourceCache claimCache;
    MySqlQueryGenerator queryGenerator = new MySqlQueryGenerator();
    @Inject
    private KubernetesClient client;

    @Inject
    private MySqlClient dbClient;

    void runWatch() {
        new Thread(() -> claimCache.listThenWatch(this::handleEvent)).start();
    }

    private void handleEvent(Watcher.Action action, String uid) {
        try {
            DataStoreAccessClaimResource resource = claimCache.get(uid);
            if (resource == null) {
                return;
            }
            onClaim(action, resource);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void onClaim(Watcher.Action action, DataStoreAccessClaimResource claim) throws SQLException {
        List<String> matchSecretNames = claim.getSpec().getSecretSelector().getMatchNames();
        List<Secret> secrets = matchSecretNames.stream()
                .map(this::getSecret)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (secrets.size() != matchSecretNames.size()) {
            LOGGER.infof("claim '%s' referenced missing secrets", claim.getMetadata().getName());
        }
        String dbName = claim.getSpec().getDataStoreSelector().getMatchName();

        List<String> allStatements = secrets.stream().flatMap(secret -> {
            String username = getDecodedSecretValue(secret, "username");
            String password = getDecodedSecretValue(secret, "password");
            List<String> statements = new LinkedList<>(queryGenerator.createOrAlterUser(username, password));
            statements.add(queryGenerator.grantPrivileges(dbName, username, claim.getSpec().getRoles()));
            return statements.stream();
        }).collect(Collectors.toList());

        dbClient.execute(allStatements);
    }


    private Optional<Secret> getSecret(String name) {
        Resource<Secret, DoneableSecret> secretRef = client.secrets().withName(name);
        if (secretRef == null) {
            return Optional.empty();
        }
        Secret secret = secretRef.get();
        return Optional.ofNullable(secret);
    }

    private String getDecodedSecretValue(Secret secret, String key) {
        Map<String, String> data = secret.getData();
        String value = data.get(key);
        return value == null ? null : new String(Base64.getDecoder().decode(value));
    }
}
