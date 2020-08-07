package org.freemountain.operator.kubernetes.specs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@JsonDeserialize
@RegisterForReflection
public class MatchNamesSelector {
    @JsonProperty("matchNames")
    private List<String> matchNames;
    
    public List<String> getMatchNames() {
        return matchNames;
    }

    public void setMatchNames( List<String> matchNames) {
        this.matchNames = matchNames;
    }

    @Override
    public String toString() {
        return "MatchNamesSelector{" +
                "matchNames='" + matchNames + '\'' +
                '}';
    }
}
