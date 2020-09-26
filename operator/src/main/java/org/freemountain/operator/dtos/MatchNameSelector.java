package org.freemountain.operator.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonDeserialize
@RegisterForReflection
public class MatchNameSelector {
    @JsonProperty("matchName")
    private String matchName;

   public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
       this.matchName = matchName;
    }


    @Override
    public String toString() {
        return "MatchNameSelector{" +
                "matchName='" + matchName + '\'' +
                '}';
    }
}
