package org.freemountain.operator.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

@JsonDeserialize
@RegisterForReflection
@JsonPropertyOrder({
       // "apiVersion",
       // "kind",
       // "metadata",
      //  "lastProbeTime",
        "lastTransitionTime",
        //"message",
        //"reason",
        "status",
        "type"
})
public class BaseCondition implements Comparable<Object> {
    @JsonProperty("lastTransitionTime")
    protected String lastTransitionTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);

    @JsonProperty("status")
    protected String status = "True";

    @JsonProperty("type")
    protected String type;

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

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BaseCondition{" +
                "lastTransitionTime='" + lastTransitionTime + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        return Comparator
                .comparing(BaseCondition::getLastTransitionTime)
                .thenComparing(BaseCondition::getStatus)
                .thenComparing(BaseCondition::getType)
                .compare(this, (BaseCondition) o);
    }
}
