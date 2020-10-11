package org.freemountain.operator.crds;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.HasBaseStatus;
import org.freemountain.operator.dtos.DataStoreSpec;
import org.freemountain.operator.dtos.BaseStatus;

@JsonDeserialize
@RegisterForReflection
public class DataStoreResource extends CustomResource implements HasBaseStatus {
    private DataStoreSpec spec;

    private BaseStatus status;

    public DataStoreResource() {
        super(CRD.DataStore.KIND);
        setApiVersion(CRD.API_VERSION);
    }

    public DataStoreSpec getSpec() {
        return spec;
    }

    public void setSpec(DataStoreSpec spec) {
        this.spec = spec;
    }

    public BaseStatus getStatus() {
        return status;
    }

    public void setStatus(BaseStatus status) {
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
