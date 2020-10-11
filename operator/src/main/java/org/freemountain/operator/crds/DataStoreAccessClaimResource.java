package org.freemountain.operator.crds;

import io.fabric8.kubernetes.client.CustomResource;
import org.freemountain.operator.common.CRD;
import org.freemountain.operator.common.HasBaseStatus;
import org.freemountain.operator.dtos.BaseStatus;
import org.freemountain.operator.dtos.DataStoreAccessClaimSpec;

public class DataStoreAccessClaimResource extends CustomResource implements HasBaseStatus {
    private BaseStatus status;
    private DataStoreAccessClaimSpec spec;

    public DataStoreAccessClaimResource() {
        super(CRD.DataStoreAccessClaim.KIND);
        setApiVersion(CRD.API_VERSION);
    }

    public DataStoreAccessClaimSpec getSpec() {
        return spec;
    }

    public void setSpec(DataStoreAccessClaimSpec spec) {
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
        return "DataStoreAccessClaimResource{" +
                "spec=" + spec +
                '}';
    }
}
