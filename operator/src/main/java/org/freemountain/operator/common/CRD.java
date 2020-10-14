package org.freemountain.operator.common;

public class CRD {
    public static final String SCOPE = "Namespaced";
    public static final String VERSION = "v1alpha1";
    public static final String API_VERSION = Constants.API_GROUP + "/" + VERSION;

    // DataStore
    public static final String DATA_STORE_KIND = "DataStore";
    public static final String DATA_STORE_PLURAL = "datastores";
    public static final String DATA_STORE_NAME = DATA_STORE_PLURAL + "." + Constants.API_GROUP;

    // DataStoreAccessClaim
    public static final String ACCESS_CLAIM_KIND = "DataStoreAccessClaim";
    public static final String ACCESS_CLAIM_PLURAL = "datastoreaccessclaims";
    public static final String ACCESS_CLAIM_NAME = ACCESS_CLAIM_PLURAL + "." + Constants.API_GROUP;
}
