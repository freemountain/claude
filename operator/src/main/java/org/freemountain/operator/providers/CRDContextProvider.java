package org.freemountain.operator.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.CRDContext;
import org.freemountain.operator.common.Constants;
import org.freemountain.operator.common.HasBaseStatus;
import org.freemountain.operator.crds.DataStoreAccessClaimResource;
import org.freemountain.operator.crds.DataStoreResource;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

public class CRDContextProvider {

    @Inject
    ObjectMapper objectMapper;


    @Produces
    @Singleton
    CRDContext<DataStoreResource> createDataStoreCtx() {
        CustomResourceDefinitionContext config = new CustomResourceDefinitionContext.Builder()
                .withName(CRD.DataStore.NAME)
                .withGroup(Constants.API_GROUP)
                .withScope(CRD.SCOPE)
                .withVersion(CRD.VERSION)
                .withKind(CRD.DataStore.KIND)
                .withPlural(CRD.DataStore.PLURAL)
                .build();

        return new CRDContext<DataStoreResource>(DataStoreResource.class, config, objectMapper);
    }

    @Produces
    @Singleton
    CRDContext<DataStoreAccessClaimResource> createDataStoreAccessClaimCtx() {
        CustomResourceDefinitionContext config = new CustomResourceDefinitionContext.Builder()
                .withName(CRD.DataStoreAccessClaim.NAME)
                .withGroup(Constants.API_GROUP)
                .withScope(CRD.SCOPE)
                .withVersion(CRD.VERSION)
                .withKind(CRD.DataStoreAccessClaim.KIND)
                .withPlural(CRD.DataStoreAccessClaim.PLURAL)
                .build();

        return new CRDContext<DataStoreAccessClaimResource>(DataStoreAccessClaimResource.class, config, objectMapper);
    }
}
