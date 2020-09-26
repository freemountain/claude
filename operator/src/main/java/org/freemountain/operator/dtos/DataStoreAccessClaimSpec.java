package org.freemountain.operator.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@JsonDeserialize
@RegisterForReflection
public class DataStoreAccessClaimSpec {
    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("dataStoreSelector")
    private MatchNameSelector dataStoreSelector;

    @JsonProperty("secretSelector")
    private MatchNamesSelector secretSelector;

    public List<String> getRoles() {
        return roles;
    }

    public MatchNameSelector getDataStoreSelector() {
        return dataStoreSelector;
    }

    public MatchNamesSelector getSecretSelector() {
        return secretSelector;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setDataStoreSelector(MatchNameSelector dataStoreSelector) {
        this.dataStoreSelector = dataStoreSelector;
    }

    public void setSecretSelector(MatchNamesSelector secretSelector) {
        this.secretSelector = secretSelector;
    }

    @Override
    public String toString() {
        return "DataStoreAccessClaimSpec{" +
                "roles=" + roles +
                ", dataStoreSelector=" + dataStoreSelector +
                ", secretSelector=" + secretSelector +
                '}';
    }
}
