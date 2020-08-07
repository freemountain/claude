package org.freemountain.operator.kubernetes.resources;

import io.fabric8.kubernetes.client.CustomResource;
import org.freemountain.operator.kubernetes.specs.DataStoreAccessClaimSpec;

public class DataStoreAccessClaimResource extends CustomResource {
    private DataStoreAccessClaimSpec spec;

    public DataStoreAccessClaimSpec getSpec() {
        return spec;
    }

    public void setSpec(DataStoreAccessClaimSpec spec) {
        this.spec = spec;
    }

    @Override
    public String toString() {
        return "DataStoreAccessClaimResource{" +
                "spec=" + spec +
                '}';
    }
}
