package org.freemountain.operator.common;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.dtos.BaseCondition;
import org.freemountain.operator.dtos.BaseStatus;
import org.freemountain.operator.operators.DataStoreOperator;
import org.jboss.logging.Logger;

import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConditionUtils {
    private static final Logger LOGGER = Logger.getLogger(ConditionUtils.class);

    public static String TRUE = "True";
    public static String FALSE = "False";
    public static String READY_CONDITION_NAME = "Ready";


    public static boolean isTrue(Collection<BaseCondition> conditions, String type) {
        return conditions.stream()
                .filter(c -> type.equals(c.getType()))
                .findFirst()
                .map(c -> TRUE.equals(c.getType()))
                .orElse(false);
    }


    public static List<BaseCondition> set(Collection<BaseCondition> conditions, BaseCondition condition) {
        var result = (conditions == null ? Collections.<BaseCondition>emptyList() : conditions).stream()
                .filter(c -> !c.getType().equals(condition.getType()))
                .collect(Collectors.toCollection(LinkedList::new));

        result.add(condition);

        return result;
    }
}
