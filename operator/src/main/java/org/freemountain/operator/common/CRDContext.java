package org.freemountain.operator.common;

import static org.freemountain.operator.common.CRD.API_VERSION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.lang.reflect.InvocationTargetException;

public class CRDContext<T extends HasBaseStatus & HasMetadata> {
    private final Class<T> crdClass;
    private final CustomResourceDefinitionContext config;
    private final ObjectMapper objectMapper;

    public CRDContext(
            Class<T> crdClass, CustomResourceDefinitionContext config, ObjectMapper objectMapper) {
        this.crdClass = crdClass;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public T clone(T target) {
        try {
            String json = objectMapper.writeValueAsString(target);
            return objectMapper.readValue(json, crdClass);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("copy failed", e);
        }
    }

    public Class<T> getCRDClass() {
        return crdClass;
    }

    public T createInstance() {
        try {
            return crdClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException("no empty default constructor found");
        }
    }

    public CustomResourceDefinitionContext getConfig() {
        return config;
    }

    public String getApiVersion() {
        return API_VERSION;
    }

    public boolean isResource(OwnerReference resource) {
        return config.getKind().equals(resource.getKind())
                && getApiVersion().equals(resource.getApiVersion());
    }
}
