package org.freemountain.operator.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.freemountain.operator.operators.DataStoreOperator;
import org.jboss.logging.Logger;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonUtils {
    private static final Logger LOGGER = Logger.getLogger(JsonUtils.class);

    public static boolean isValueNode(JsonNode node) {
        switch (node.getNodeType()) {
            case NULL: ;
            case NUMBER:
            case STRING:
            case BOOLEAN: return true;
            default: return false;
        }
    }

    public static JsonNode filterEmptyContainer(JsonNode container) {
        if(container.isContainerNode() && container.size() == 0) {
            return null;
        }
        return container;
    }

    public static ValueNode filterNullNode(ValueNode value) {
        if(value.isNull()) {
            return null;
        }
        return value;
    }

    public static JsonNode normalizeWithoutEmptyContainerAndNull(JsonNode node) {
        return normalize(node, JsonUtils::filterNullNode, JsonUtils::filterEmptyContainer);
    }

    public static JsonNode normalize(JsonNode node) {
        return normalize(node, v -> v, c -> c);
    }

    public static JsonNode normalize(JsonNode node, Function<ValueNode, ValueNode> onValue, Function<JsonNode, JsonNode> onContainer) {
        if(node == null || node.isMissingNode()) {
            return null;
        }
        if(isValueNode(node)) {
            return onValue.apply((ValueNode) node);
        }

        if(node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            LinkedList<Integer> toRemove = new LinkedList<>();
            for(int i = 0; i < arrayNode.size(); i++) {
                JsonNode normalized = normalize(arrayNode.get(i), onValue, onContainer);
                if(normalized == null) {
                    toRemove.add(i);
                    continue;
                }
                arrayNode.set(i, normalized);
            }
            for(int i : toRemove) {
                arrayNode.remove(i);
            }
        } else if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            LinkedList<String> toRemove = new LinkedList<>();
            objectNode.fieldNames().forEachRemaining(key -> {
                JsonNode normalized = normalize(objectNode.get(key), onValue, onContainer);
                if(normalized == null) {
                    toRemove.add(key);
                    return;
                }
                objectNode.set(key, normalized);
            });
            for(String key : toRemove) {
                objectNode.remove(key);
            }
        } else {
            LOGGER.warnf("Unknown jsonNode type %s", node.getNodeType());
            return null;
        }
        return onContainer.apply(node);

    }

    public static <T extends Comparable<Object>> ArrayNode sortWithType(ObjectMapper mapper, ArrayNode target, Class<T> type) {
        return sortWithType(mapper, target, type, Comparable::compareTo);
    }
    public static <T> ArrayNode sortWithType(ObjectMapper mapper, ArrayNode target, Class<T> type, Comparator<T> comparator) {
        List<JsonNode> sorted= StreamSupport
                .stream(target.spliterator(),  false)
                .map( element -> mapper.convertValue(element, type))
                .sorted(comparator)
                .map( element -> mapper.convertValue(element, JsonNode.class))
                .collect(Collectors.toList());

        target.removeAll().addAll(sorted);

        return target;
    }
}
