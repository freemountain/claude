package org.freemountain.operator.crds;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.freemountain.operator.dtos.DataStoreSpec;
import org.freemountain.operator.dtos.DataStoreStatus;

@JsonDeserialize
@RegisterForReflection
public class DataStoreResource extends CustomResource {
    @JsonProperty("spec")
    private DataStoreSpec spec;

    @JsonProperty("status")
    private DataStoreStatus status;

    public DataStoreResource() {
        super("DataStore");
        setApiVersion("instana.com/v1alpha1");
    }

    public DataStoreSpec getSpec() {
        return spec;
    }

    public void setSpec(DataStoreSpec spec) {
        this.spec = spec;
    }

    public DataStoreStatus getStatus() {
        return status;
    }

    public void setStatus(DataStoreStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DataStoreResource{" +
                "spec=" + spec +
                ", status=" + status +
                "} " + super.toString();
    }
}
