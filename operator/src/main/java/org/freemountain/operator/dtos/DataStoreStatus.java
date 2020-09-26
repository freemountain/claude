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
public class DataStoreStatus implements KubernetesResource  {
    @JsonProperty("test")
    private String test;

    @JsonProperty("test")
    public String getTest() {
        return test;
    }

    @JsonProperty("test")
    public void setTest(String test) {
        this.test = test;
    }

    @Override
    public String toString() {
        return "DataStoreStatus{" +
                "test='" + test + '\'' +
                '}';
    }

    /*
    public enum DataStoreConditionType {

    }
    @JsonDeserialize
    @RegisterForReflection
    public static class DataStoreCondition extends BaseCondition {
        @JsonProperty("type")
        private DataStoreConditionType type;

        @JsonProperty("type")
        public DataStoreConditionType getType() {
            return type;
        }

        @JsonProperty("type")
        public void setType(DataStoreConditionType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "DataStoreCondition{" +
                    "type=" + type +
                    "} " + super.toString();
        }
    }

    @JsonProperty("conditions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<DataStoreCondition> conditions = new ArrayList<>();

    @JsonProperty("conditions")
    public List<DataStoreCondition> getConditions() {
        return conditions;
    }

    @JsonProperty("conditions")
    public void setConditions(List<DataStoreCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String toString() {
        return "DataStoreStatus{" +
                "conditions=" + conditions +
                '}';
    }

     */
}
