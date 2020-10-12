package org.freemountain.operator.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.CRDContext;
import org.freemountain.operator.common.Constants;
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
                .withName(CRD.DATA_STORE_NAME)
                .withGroup(Constants.API_GROUP)
                .withScope(CRD.SCOPE)
                .withVersion(CRD.VERSION)
                .withKind(CRD.DATA_STORE_KIND)
                .withPlural(CRD.DATA_STORE_PLURAL)
                .build();

        return new CRDContext<DataStoreResource>(DataStoreResource.class, config, objectMapper);
    }

    @Produces
    @Singleton
    CRDContext<DataStoreAccessClaimResource> createDataStoreAccessClaimCtx() {
        CustomResourceDefinitionContext config = new CustomResourceDefinitionContext.Builder()
                .withName(CRD.ACCESS_CLAIM_NAME)
                .withGroup(Constants.API_GROUP)
                .withScope(CRD.SCOPE)
                .withVersion(CRD.VERSION)
                .withKind(CRD.ACCESS_CLAIM_KIND)
                .withPlural(CRD.ACCESS_CLAIM_PLURAL)
                .build();

        return new CRDContext<DataStoreAccessClaimResource>(DataStoreAccessClaimResource.class, config, objectMapper);
    }
}
