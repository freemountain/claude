package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import java.util.Optional;

public class CRD {
    public static final String SCOPE = "Namespaced";
    public static final String VERSION = "v1alpha1";
    public static final String API_VERSION = Constants.API_GROUP + "/" + VERSION;

    public enum Type {
        DATA_STORE(DataStore.CONTEXT),
        DATA_STORE_ACCESS_CLAIM(DataStoreAccessClaim.CONTEXT);

        private final CustomResourceDefinitionContext context;

        Type(CustomResourceDefinitionContext context) {
            this.context = context;
        }

        public static Optional<Type> from(OwnerReference owner) {
            String kind = owner.getKind();

            if(!API_VERSION.equals(owner.getApiVersion())) {
                return Optional.empty();
            }

            if (DataStore.KIND.equals(kind)) {
                return Optional.of(DATA_STORE);
            }

            if (DataStoreAccessClaim.KIND.equals(kind)) {
                return Optional.of(DATA_STORE_ACCESS_CLAIM);
            }

            return Optional.empty();
        }

        public final CustomResourceDefinitionContext getContext() {
            return context;
        }
    }

    public static final class DataStore {
        public static final String KIND = "DataStore";
        public static final String PLURAL = "datastores";
        public static final String NAME = PLURAL + "." + Constants.API_GROUP;

        public static final CustomResourceDefinitionContext CONTEXT = new CustomResourceDefinitionContext.Builder()
                .withName(NAME)
                .withGroup(Constants.API_GROUP)
                .withScope(SCOPE)
                .withVersion(VERSION)
                .withKind(KIND)
                .withPlural(PLURAL)
                .build();
    }

    public static class DataStoreAccessClaim {
        public static final String KIND = "DataStoreAccessClaim";
        public static final String PLURAL = "datastoreaccessclaims";
        public static final String NAME = PLURAL + "." + Constants.API_GROUP;

        public static final CustomResourceDefinitionContext CONTEXT = new CustomResourceDefinitionContext.Builder()
                .withName(NAME)
                .withGroup(Constants.API_GROUP)
                .withScope(SCOPE)
                .withVersion(VERSION)
                .withKind(KIND)
                .withPlural(PLURAL)
                .build();
    }


}
