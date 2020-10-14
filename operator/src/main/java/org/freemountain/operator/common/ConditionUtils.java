package org.freemountain.operator.common;

import java.util.*;
import java.util.stream.Collectors;
import org.freemountain.operator.dtos.BaseCondition;
import org.jboss.logging.Logger;

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

    public static List<BaseCondition> set(
            Collection<BaseCondition> conditions, BaseCondition condition) {
        var result =
                (conditions == null ? Collections.<BaseCondition>emptyList() : conditions)
                        .stream()
                                .filter(c -> !c.getType().equals(condition.getType()))
                                .collect(Collectors.toCollection(LinkedList::new));

        result.add(condition);

        return result;
    }
}
