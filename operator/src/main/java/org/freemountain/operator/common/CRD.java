package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import java.util.Optional;

public class CRD {
    public static final String SCOPE = "Namespaced";
    public static final String VERSION = "v1alpha1";
    public static final String API_VERSION = Constants.API_GROUP + "/" + VERSION;

    public static final class DataStore {
        public static final String KIND = "DataStore";
        public static final String PLURAL = "datastores";
        public static final String NAME = PLURAL + "." + Constants.API_GROUP;
    }

    public static class DataStoreAccessClaim {
        public static final String KIND = "DataStoreAccessClaim";
        public static final String PLURAL = "datastoreaccessclaims";
        public static final String NAME = PLURAL + "." + Constants.API_GROUP;
    }
}

