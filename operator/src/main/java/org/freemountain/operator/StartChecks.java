package org.freemountain.operator;


import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

public class StartChecks {

    /*@Inject
    private KubernetesClient client;
*/

    void onStartup(@Observes StartupEvent _ev) {
        /*
        List<Pod> podList = client.pods().list().getItems();
        System.out.println("Found " + podList.size() + " Pods!!:");
        for (Pod pod : podList) {
            System.out.println(" * " + pod.getMetadata().getName());
        }

        for (String name : new String[]{"MYSQL_USER", "MYSQL_PASSWORD", "MYSQL_HOST", "MYSQL_PORT"}) {
            System.out.println(name + ": " + System.getenv(name));
        }
        */
    }
}