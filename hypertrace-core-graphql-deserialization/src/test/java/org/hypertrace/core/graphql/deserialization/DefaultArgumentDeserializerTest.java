package org.hypertrace.core.graphql.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultArgumentDeserializerTest {

  private DefaultArgumentDeserializer argumentDeserializer;

  private interface TestPrimitiveArgument extends PrimitiveArgument<String> {
    String ARG_NAME = "primitiveArg";
  }

  private interface TestObjectArgument {
    String ARG_NAME = "objectArg";
    String VALUE_NAME = "value";
  }

  private static class DefaultTestObjectArgument implements TestObjectArgument {
    @JsonProperty(VALUE_NAME)
    private String value;

    private DefaultTestObjectArgument(String value) {
      this.value = value;
    }

    DefaultTestObjectArgument() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DefaultTestObjectArgument that = (DefaultTestObjectArgument) o;
      return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }

  @BeforeEach
  void beforeEach() {
    this.argumentDeserializer =
        new DefaultArgumentDeserializer(
            Set.of(
                new ArgumentDeserializationConfig() {
                  @Override
                  public String getArgumentKey() {
                    return TestPrimitiveArgument.ARG_NAME;
                  }

                  @Override
                  public Class<?> getArgumentSchema() {
                    return TestPrimitiveArgument.class;
                  }
                },
                new ArgumentDeserializationConfig() {
                  @Override
                  public String getArgumentKey() {
                    return TestObjectArgument.ARG_NAME;
                  }

                  @Override
                  public Class<?> getArgumentSchema() {
                    return TestObjectArgument.class;
                  }

                  @Override
                  public List<Module> jacksonModules() {
                    return List.of(
                        new SimpleModule()
                            .addAbstractTypeMapping(
                                TestObjectArgument.class, DefaultTestObjectArgument.class));
                  }
                }));
  }

  @Test
  void deserializesObjectListIfPresent() {
    Map<String, Object> argMap =
        Map.of(
            TestObjectArgument.ARG_NAME,
            List.of(
                Map.of(TestObjectArgument.VALUE_NAME, "foo"),
                Map.of(TestObjectArgument.VALUE_NAME, "bar")));

    List<TestObjectArgument> result =
        this.argumentDeserializer
            .deserializeObjectList(argMap, TestObjectArgument.class)
            .orElseThrow();
    assertEquals(2, result.size());
    assertEquals(new DefaultTestObjectArgument("foo"), result.get(0));
    assertEquals(new DefaultTestObjectArgument("bar"), result.get(1));
  }

  @Test
  void emptyObjectListIfNotPresent() {
    assertEquals(
        Optional.empty(),
        this.argumentDeserializer.deserializeObjectList(
            Collections.emptyMap(), TestObjectArgument.class));
  }

  @Test
  void emptyObjectListIfUnableToDeserialize() {
    Map<String, Object> argMap =
        Map.of(TestObjectArgument.ARG_NAME, List.of(Map.of("garbage", "more garbage")));

    assertEquals(
        Optional.empty(),
        this.argumentDeserializer.deserializeObjectList(argMap, TestObjectArgument.class));
  }

  @Test
  void emptyListIfObjectListPresentButEmpty() {
    Map<String, Object> argMap = Map.of(TestObjectArgument.ARG_NAME, List.of());
    assertEquals(
        Optional.of(List.of()),
        this.argumentDeserializer.deserializeObjectList(argMap, TestObjectArgument.class));
  }

  @Test
  void deserializeObjectIfPresent() {
    Map<String, Object> argMap =
        Map.of(TestObjectArgument.ARG_NAME, Map.of(TestObjectArgument.VALUE_NAME, "baz"));

    assertEquals(
        new DefaultTestObjectArgument("baz"),
        this.argumentDeserializer
            .deserializeObject(argMap, TestObjectArgument.class)
            .orElseThrow());
  }

  @Test
  void emptyIfNoObjectPresent() {
    assertEquals(
        Optional.empty(),
        this.argumentDeserializer.deserializeObject(
            Collections.emptyMap(), TestObjectArgument.class));
  }

  @Test
  void emptyIfDeserializationFailurePresent() {
    Map<String, Object> argMap =
        Map.of(TestObjectArgument.ARG_NAME, Map.of("garbage", "more garbage"));

    assertEquals(
        Optional.empty(),
        this.argumentDeserializer.deserializeObject(argMap, TestObjectArgument.class));
  }

  @Test
  void deserializePrimitive() {
    Map<String, Object> argMap = Map.of(TestPrimitiveArgument.ARG_NAME, "baz");

    assertEquals(
        Optional.of("baz"),
        this.argumentDeserializer.deserializePrimitive(argMap, TestPrimitiveArgument.class));
  }

  @Test
  void emptyIfNoPrimitivePresent() {
    assertEquals(
        Optional.empty(),
        this.argumentDeserializer.deserializePrimitive(
            Collections.emptyMap(), TestPrimitiveArgument.class));
  }

  @Test
  void deserializePrimitiveList() {
    Map<String, Object> argMap =
        Map.of(TestPrimitiveArgument.ARG_NAME, List.of("foo", "bar", "baz"));
    assertEquals(
        Optional.of(List.of("foo", "bar", "baz")),
        this.argumentDeserializer.deserializePrimitiveList(argMap, TestPrimitiveArgument.class));
  }

  @Test
  void emptyListForEmptyPrimitiveList() {
    Map<String, Object> argMap = Map.of(TestPrimitiveArgument.ARG_NAME, List.of());
    assertEquals(
        Optional.of(List.of()),
        this.argumentDeserializer.deserializePrimitiveList(argMap, TestPrimitiveArgument.class));
  }

  @Test
  void emptyIfNoPrimitiveList() {
    assertEquals(
        Optional.empty(),
        this.argumentDeserializer.deserializePrimitiveList(
            Collections.emptyMap(), TestPrimitiveArgument.class));
  }

  @Test
  void emptyIfNotList() {
    Map<String, Object> argMap = Map.of(TestPrimitiveArgument.ARG_NAME, "baz");
    assertEquals(
        Optional.empty(),
        this.argumentDeserializer.deserializePrimitiveList(argMap, TestPrimitiveArgument.class));
  }

  @Test
  void allowsArgNameOverride() {

    Map<String, Object> argMap =
        Map.of(
            "custom-primitive",
            "baz",
            "custom-primitive-list",
            List.of("baz"),
            "custom-object",
            Map.of(TestObjectArgument.VALUE_NAME, "baz"),
            "custom-object-list",
            List.of(Map.of(TestObjectArgument.VALUE_NAME, "baz")));

    assertEquals(
        Optional.of("baz"),
        this.argumentDeserializer.deserializePrimitive(argMap, "custom-primitive"));
    assertEquals(
        Optional.of(List.of("baz")),
        this.argumentDeserializer.deserializePrimitiveList(argMap, "custom-primitive-list"));
    assertEquals(
        Optional.of(new DefaultTestObjectArgument("baz")),
        this.argumentDeserializer.deserializeObject(
            argMap, TestObjectArgument.class, "custom-object"));
    assertEquals(
        Optional.of(List.of(new DefaultTestObjectArgument("baz"))),
        this.argumentDeserializer.deserializeObjectList(
            argMap, TestObjectArgument.class, "custom-object-list"));
  }
}
