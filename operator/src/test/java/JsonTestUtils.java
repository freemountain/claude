import com.fasterxml.jackson.databind.JsonNode;
import org.opentest4j.AssertionFailedError;

public class JsonTestUtils {
    public static void assertJsonHashCodeEquals(JsonNode expected, JsonNode actual) {
        assertJsonHashCodeEquals(expected, actual, "");
    }

    public static void assertJsonHashCodeEquals(
            JsonNode expected, JsonNode actual, String message) {
        if (expected == actual) {
            return;
        } else if (actual == null) {
            throw new AssertionFailedError(message, expected.toString(), null);
        } else if (expected == null) {
            throw new AssertionFailedError(message, null, actual.toString());
        }
        int actualHashCode = actual.hashCode();
        int expectedHashCode = expected.hashCode();

        if (actualHashCode != expectedHashCode) {
            throw new AssertionFailedError(
                    "Hashcode did not match" + message, expected.toString(), actual.toString());
        }
    }
}
