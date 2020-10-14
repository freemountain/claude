import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.util.LinkedList;
import java.util.UUID;
import org.freemountain.operator.common.HasBaseStatus;
import org.freemountain.operator.common.ResourceHash;
import org.freemountain.operator.crds.DataStoreResource;
import org.freemountain.operator.dtos.BaseCondition;
import org.freemountain.operator.dtos.BaseStatus;
import org.freemountain.operator.dtos.DataStoreSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceHashTest {
    ObjectMapper mapper = new ObjectMapper();

    public static void addMetadata(HasMetadata target) {
        ObjectMeta meta = target.getMetadata() == null ? new ObjectMeta() : target.getMetadata();
        meta.setNamespace("default");
        meta.setUid(UUID.randomUUID().toString());
        meta.setGeneration((long) 100);
        target.setMetadata(meta);
    }

    public static void addConditions(HasBaseStatus target) {
        BaseStatus status = target.getStatus() == null ? new BaseStatus() : target.getStatus();
        target.setStatus(status);

        if (status.getConditions() == null) {
            status.setConditions(new LinkedList<>());
        }
    }

    @Test
    public void test() {
        BaseCondition conditionA = new BaseCondition();
        conditionA.setType("ConditionA");
        conditionA.setStatus("True");
        BaseCondition conditionB = new BaseCondition();
        conditionA.setType("ConditionB");
        conditionA.setStatus("False");

        DataStoreResource storeFoo = new DataStoreResource();
        addMetadata(storeFoo);
        addConditions(storeFoo);
        storeFoo.getMetadata().setName("store-foo");
        storeFoo.setSpec(new DataStoreSpec());
        storeFoo.getSpec().setName("store-foo-name");
        storeFoo.getSpec().setProvider("provider-a");
        storeFoo.getStatus().getConditions().add(conditionA);
        storeFoo.getStatus().getConditions().add(conditionB);

        DataStoreResource storeFooB = new DataStoreResource();
        storeFooB.setMetadata(storeFoo.getMetadata());
        addConditions(storeFooB);

        storeFooB.setSpec(new DataStoreSpec());
        storeFooB.getSpec().setProvider("provider-a");
        storeFooB.getSpec().setName("store-foo-name");
        storeFooB.getStatus().getConditions().add(conditionB);
        storeFooB.getStatus().getConditions().add(conditionA);

        ResourceHash fooHash = ResourceHash.hash(mapper, storeFoo);
        ResourceHash fooBHash = ResourceHash.hash(mapper, storeFooB);

        Assertions.assertEquals(fooHash, fooBHash);
    }
}
