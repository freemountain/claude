package org.freemountain.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.freemountain.operator.providers.DataStoreConfigProvider;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@ApplicationScoped
public class StartChecks {
    private static final Logger LOG = Logger.getLogger(StartChecks.class);

    @Inject
    @Named("namespace")
    String namespace;

    @Inject
    DataStoreConfigProvider configProvider;

    void onStartup(@Observes StartupEvent _ev) {

        LOG.infof("namespace %s", namespace);

       // LOG.infof("sss %s", configProvider.getConfig("test"));

       //s op.runWatch();
                /*
        for (String name : new String[]{"MYSQL_USER", "MYSQL_PASSWORD", "MYSQL_HOST", "MYSQL_PORT"}) {
            System.out.println(name + ": " + System.getenv(name));
        }
        */
    }
}