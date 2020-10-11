package org.freemountain.operator.operators;

import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.freemountain.operator.caches.DataStoreAccessClaimCacheEmitter;
import org.freemountain.operator.caches.DataStoreCacheEmitter;
import org.freemountain.operator.common.*;
import org.freemountain.operator.crds.*;
import org.freemountain.operator.dtos.BaseCondition;
import org.freemountain.operator.dtos.DataStoreUser;
import org.freemountain.operator.events.DataStoreAccessClaimLifecycleEvent;
import org.freemountain.operator.events.JobLifecycleEvent;
import org.freemountain.operator.templates.DataStoreJobTemplate;
import org.freemountain.operator.templates.JobTemplateService;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class DataStoreAccessClaimOperator {
    private static final Logger LOGGER = Logger.getLogger(DataStoreAccessClaimOperator.class);

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    DataStoreCacheEmitter dataStoreCache;

    @Inject
    DataStoreAccessClaimCacheEmitter dataStoreAccessClaimCacheEmitter;

    @Inject
    JobTemplateService jobTemplateService;

    @Inject
    NonNamespaceOperation<DataStoreAccessClaimResource, DataStoreAccessClaimResourceList, DataStoreAccessClaimResourceDoneable, Resource<DataStoreAccessClaimResource, DataStoreAccessClaimResourceDoneable>> dataStoreAccessClaimResourceClient;


    @Incoming(JobLifecycleEvent.ADDRESS)
    void onJobEvent(JobLifecycleEvent event) {
        if (!event.isFinishedEvent()) {
            return;
        }

        var dataStoreAccessClaimResource = jobTemplateService.getOwningResource(CRD.Type.DATA_STORE_ACCESS_CLAIM, event.getResource())
                .map(OwnerReference::getUid)
                .flatMap(dataStoreAccessClaimCacheEmitter::get)
                .orElse(null);

        if (dataStoreAccessClaimResource == null) {
            return;
        }

        LOGGER.debugf("Job for dataStoreAccessClaimResource '%s' changed to %s", dataStoreAccessClaimResource.getMetadata().getName(), event.getJobState());

        BaseCondition condition = new BaseCondition();
        condition.setType(ConditionUtils.READY_CONDITION_NAME);
        condition.setStatus(ConditionUtils.TRUE);

        if (event.getJobState().equals(JobState.FAILED)) {
            condition.setStatus(ConditionUtils.FALSE);
        }

        var conditions = dataStoreAccessClaimResource.getStatus() != null ? dataStoreAccessClaimResource.getStatus().getConditions() : null;

        ConditionUtils.updateConditions(dataStoreAccessClaimResourceClient::updateStatus, DataStoreAccessClaimResource::new, dataStoreAccessClaimResource, ConditionUtils.set(conditions, condition));
  }

    @Incoming(DataStoreAccessClaimLifecycleEvent.ADDRESS)
    void onEvent(DataStoreAccessClaimLifecycleEvent event) {
        DataStoreAccessClaimResource resource = event.getResource();
        if (!event.shouldCreateJob()) {
            return;
        }

        var spec = event.getResource().getSpec();

        Optional<Secret> matchedSecret = getSecret(spec.getSecretSelector().getMatchName());
        Optional<DataStoreResource> matchedDataStore = dataStoreCache.values()
                .stream()
                .filter(e -> e.getMetadata().getName().equals(spec.getDataStoreSelector().getMatchName()))
                .findFirst();

        if(matchedSecret.isEmpty() || matchedDataStore.isEmpty()) {
            if(matchedSecret.isEmpty()){
                LOGGER.errorf("Could not find secret with name '%s'", spec.getSecretSelector().getMatchName());
            }
            if(matchedDataStore.isEmpty()){
                LOGGER.errorf("Could not find dataStore with name '%s'", spec.getDataStoreSelector().getMatchName());
            }
            return;
        }

        String username = getDecodedSecretValue(matchedSecret.get(), "username");
        String password = getDecodedSecretValue(matchedSecret.get(), "password");
        DataStoreUser user = new DataStoreUser(username, password, spec.getRoles());
        Optional<DataStoreJobTemplate> jobTemplate = jobTemplateService.buildCreateUserTemplate(matchedDataStore.get(),resource, user);

        if (jobTemplate.isEmpty()) {
            LOGGER.error("Provider '%s' is not configured");
            System.exit(-1);
            return;

        }
        kubernetesClient.batch().jobs().create(jobTemplate.get().getJob());
    }


    private  Optional<Secret> getSecret(String name) {
        Resource<Secret, DoneableSecret> secretRef = kubernetesClient.secrets().withName(name);
        if (secretRef == null) {
            return Optional.empty();
        }
        Secret secret = secretRef.get();
        return Optional.ofNullable(secret);
    }

    private static String getDecodedSecretValue(Secret secret, String key) {
        Map<String, String> data = secret.getData();
        String value = data.get(key);
        return value == null ? null : new String(Base64.getDecoder().decode(value));
    }
}
