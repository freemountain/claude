package org.freemountain.operator.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize
@RegisterForReflection
public class BaseStatus {
    @JsonProperty("conditions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<BaseCondition> conditions = new ArrayList<>();

    @JsonProperty("conditions")
    public List<BaseCondition> getConditions() {
        return conditions;
    }

    @JsonProperty("conditions")
    public void setConditions(List<BaseCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String toString() {
        return "DataStoreStatus{" +
                "conditions=" + conditions +
                '}';
    }
}
