package org.freemountain.operator.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonDeserialize()
@RegisterForReflection
public class DataStoreSpec implements KubernetesResource {
    @JsonProperty("provider")
    private String provider;

    @JsonProperty("name")
    private String name;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DataStoreSpec{" + "provider='" + provider + '\'' + ", name='" + name + '\'' + '}';
    }
}
