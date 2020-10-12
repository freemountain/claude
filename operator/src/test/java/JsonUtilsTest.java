import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.freemountain.operator.common.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class JsonUtilsTest {

    ObjectMapper mapper = new ObjectMapper();

    public static ArrayNode addValueProps(ArrayNode target) {
        target.add(new IntNode(1));
        target.add(new TextNode("foo"));
        target.add(new TextNode(""));
        target.add(true);
        target.add(false);
        target.add(NullNode.getInstance());

        return target;
    }

    public static ObjectNode addValueProps(ObjectNode target) {
        target.set("aNumber", new IntNode(1));
        target.set("aString", new TextNode("foo"));
        target.set("emptyString", new TextNode(""));
        target.set("trueBoolean", BooleanNode.TRUE);
        target.set("falseBoolean", BooleanNode.FALSE);
        target.set("nullValue", null);

        return target;
    }

    public static ArrayNode addEmptyContainerProps(ObjectMapper mapper, ArrayNode target) {
        target.add(mapper.createObjectNode());
        target.add(mapper.createArrayNode());

        ObjectNode nestedEmpty = mapper.createObjectNode();
        nestedEmpty.set("nestedEmptyArray", mapper.createArrayNode());

        target.add(nestedEmpty);
return target;
    }
        public static ObjectNode addEmptyContainerProps(ObjectMapper mapper, ObjectNode target) {
        target.set("emptyObject", mapper.createObjectNode());
        target.set("emptyArray", mapper.createArrayNode());

        ObjectNode nestedEmpty = mapper.createObjectNode();
        nestedEmpty.set("nestedEmptyArray", mapper.createArrayNode());

        target.set("objWithEmpty", nestedEmpty);

        return target;
    }

    public static ObjectNode createNestedObject(ObjectMapper mapper) {
        ObjectNode result = mapper.createObjectNode();
        addValueProps(result);
        addEmptyContainerProps(mapper, result);
        ArrayNode valueArray = mapper.createArrayNode();
        addValueProps(valueArray);
        addEmptyContainerProps(mapper, valueArray);
        result.set("valueArray", valueArray);

        return result;
    }

    @Test
    public void test_normalize() throws JsonProcessingException {
        Assertions.assertNull(JsonUtils.normalize(null));
        Assertions.assertNull(JsonUtils.normalize(mapper.missingNode()));
        Assertions.assertNull(JsonUtils.normalize(new POJONode(new Object())));

        JsonTestUtils.assertJsonHashCodeEquals(mapper.createObjectNode(), JsonUtils.normalize(mapper.createObjectNode()));
        JsonTestUtils.assertJsonHashCodeEquals(mapper.nullNode(), JsonUtils.normalize(mapper.nullNode()));

        JsonNode expected = createNestedObject(mapper);

        ObjectNode input = createNestedObject(mapper);
        ((ArrayNode) input.get("valueArray")).add(mapper.missingNode());
        input.set("missingNode", mapper.missingNode());
        ((ObjectNode) input.get("emptyObject")).set("pojoNode", new POJONode(LocalDateTime.now())) ;

        JsonTestUtils.assertJsonHashCodeEquals(createNestedObject(mapper), JsonUtils.normalize(input));
    }

    @Test
    public void playground() throws JsonProcessingException {
        System.out.println("nullNode " + NullNode.getInstance().hashCode());
        System.out.println("nullNode " + NullNode.getInstance().size());

    }
}
