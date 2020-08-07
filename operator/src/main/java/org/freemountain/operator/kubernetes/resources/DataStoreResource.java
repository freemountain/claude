package org.freemountain.operator.kubernetes.resources;

import io.fabric8.kubernetes.client.CustomResource;
import org.freemountain.operator.kubernetes.specs.DataStoreSpec;

public class DataStoreResource extends CustomResource {
    private DataStoreSpec spec;

    public DataStoreSpec getSpec() {
        return spec;
    }

    public void setSpec(DataStoreSpec spec) {
        this.spec = spec;
    }

    @Override
    public String toString() {
        return "DataStoreResource{" +
                "spec=" + spec +
                '}';
    }
}
