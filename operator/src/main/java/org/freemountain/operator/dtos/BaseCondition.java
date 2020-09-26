package org.freemountain.operator.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize
@RegisterForReflection
public class BaseCondition {
    @JsonProperty("lastProbeTime")
    private String lastProbeTime;

    @JsonProperty("lastTransitionTime")
    private String lastTransitionTime;

    @JsonProperty("status")
    private String status = "True";

    @JsonProperty("lastProbeTime")
    public String getLastProbeTime() {
        return lastProbeTime;
    }

    @JsonProperty("lastProbeTime")
    public void setLastProbeTime(String lastProbeTime) {
        this.lastProbeTime = lastProbeTime;
    }

    @JsonProperty("lastTransitionTime")
    public String getLastTransitionTime() {
        return lastTransitionTime;
    }

    @JsonProperty("lastTransitionTime")
    public void setLastTransitionTime(String lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BaseCondition{" +
                "lastProbeTime='" + lastProbeTime + '\'' +
                ", lastTransitionTime='" + lastTransitionTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
