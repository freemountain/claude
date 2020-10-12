package org.freemountain.operator.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.kubernetes.api.model.ManagedFieldsEntry;
import io.vertx.core.json.JsonArray;
import org.freemountain.operator.dtos.BaseCondition;

import java.util.Comparator;
import java.util.Objects;

public class ResourceHash {
    protected int metadata = 0;
    protected int spec = 0;
    protected int status = 0;

    public ResourceHash(int metadata, int spec, int status) {
        this.spec = spec;
        this.metadata = metadata;
        this.status = status;
    }

    public int getMetadata() {
        return metadata;
    }

    public int getSpec() {
        return spec;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceHash)) return false;
        ResourceHash that = (ResourceHash) o;
        return metadata == that.metadata &&
                spec == that.spec &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        if(metadata == 0&& spec == 0 && status == 0) {
            return 0;
        }
        return Objects.hash(metadata, spec, status);
    }

    @Override
    public String toString() {
        return "ResourceHash{" +
                "metadata=" + metadata +
                ", spec=" + spec +
                ", status=" + status +
                ", (hashcode= " + this.hashCode() + ")" +
                '}';
    }

    public static class Builder extends ResourceHash {
        public Builder() {
            super(0, 0, 0);
        }

        public Builder(ResourceHash other) {
            super(other.metadata, other.spec, other.status);
        }

        public Builder(int metadata, int spec, int status) {
            super(metadata, spec, status);
        }

        public void setMetadata(int metadata) {
            this.metadata = metadata;
        }

        public void setSpec(int spec) {
            this.spec = spec;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        ResourceHash build() {
            return new ResourceHash(metadata, spec, status);
        }
    }

    public static ResourceHash hash(ObjectMapper mapper, Object target) {
        ResourceHash.Builder hash = new ResourceHash.Builder();

        JsonNode node = mapper.convertValue(target, JsonNode.class);

        JsonNode metadata = JsonUtils.normalizeWithoutEmptyContainerAndNull(node.get("metadata"));
        JsonNode spec = JsonUtils.normalizeWithoutEmptyContainerAndNull(node.get("spec"));
        JsonNode status = JsonUtils.normalizeWithoutEmptyContainerAndNull(node.get("status"));

        hash.setMetadata(hashMetadata(mapper, metadata));
        hash.setSpec(hash(spec));
        hash.setStatus(hashStatus(mapper, status));

        return hash;
    }

    public static String toJson(ObjectMapper mapper, ResourceHash hash) {
        ObjectNode result = mapper.createObjectNode();
        result.set("metadata", new IntNode(hash.getMetadata()));
        result.set("spec", new IntNode(hash.getSpec()));
        result.set("status", new IntNode(hash.getStatus()));

        return result.toString();
    }

    private static int hash(JsonNode node) {
        if(node == null) {
            return 0;
        }

        return node.hashCode();
    }

    private static int hashStatus(ObjectMapper mapper, JsonNode status) {
        JsonNode normalized = JsonUtils.normalizeWithoutEmptyContainerAndNull(status);
        if(normalized == null || !normalized.isObject() || normalized.isEmpty()) {
            return 0;
        }

        JsonNode conditions = normalized.get("conditions");
        if(conditions != null && conditions.isArray()) {
            JsonUtils.sortWithType(mapper, (ArrayNode) conditions, BaseCondition.class);
        }

        return normalized.hashCode();
    }

    private static int hashMetadata(ObjectMapper mapper, JsonNode metadata) {
        JsonNode normalized = JsonUtils.normalizeWithoutEmptyContainerAndNull(metadata);
        if(normalized == null || !normalized.isObject() || normalized.isEmpty()) {
            return 0;
        }

        JsonNode managedFields = normalized.get("managedFields");
        if(managedFields != null && managedFields.isArray()) {
            var compare = Comparator.comparing(ManagedFieldsEntry::getApiVersion)
                    .thenComparing(ManagedFieldsEntry::getFieldsType)
                    //.thenComparing(ManagedFieldsEntry::getFieldsV1)
                    .thenComparing(ManagedFieldsEntry::getManager);
            JsonUtils.sortWithType(mapper, (ArrayNode) managedFields, ManagedFieldsEntry.class, compare);
        }

        return normalized.hashCode();
    }


}
