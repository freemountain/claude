package org.freemountain.operator;

import io.quarkus.runtime.StartupEvent;
import org.freemountain.operator.providers.DataStoreConfigProvider;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class StartChecks {
    private static final Logger LOG = Logger.getLogger(StartChecks.class);

@Inject
DataStoreConfigProvider configProvider;

    void onStartup(@Observes StartupEvent _ev) {

       // LOG.infof("sss %s", configProvider.getConfig("test"));

       //s op.runWatch();
                /*
        for (String name : new String[]{"MYSQL_USER", "MYSQL_PASSWORD", "MYSQL_HOST", "MYSQL_PORT"}) {
            System.out.println(name + ": " + System.getenv(name));
        }
        */
    }
}