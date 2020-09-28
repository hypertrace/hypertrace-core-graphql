package org.hypertrace.core.graphql.common.utils.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.attributes.AttributeModelType;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeType;
import org.junit.jupiter.api.Test;

public class AttributeTypeConverterTest {
  @Test
  void testConvert() {
    AttributeTypeConverter attributeTypeConverter = new AttributeTypeConverter();

    assertEquals(AttributeType.STRING,
        attributeTypeConverter.convert(AttributeModelType.STRING).blockingGet());
    assertEquals(Single.just(AttributeType.BOOLEAN).blockingGet(),
        attributeTypeConverter.convert(AttributeModelType.BOOLEAN).blockingGet());
    assertEquals(Single.just(AttributeType.LONG).blockingGet(),
        attributeTypeConverter.convert(AttributeModelType.LONG).blockingGet());
    assertEquals(Single.just(AttributeType.DOUBLE).blockingGet(),
        attributeTypeConverter.convert(AttributeModelType.DOUBLE).blockingGet());
    assertEquals(Single.just(AttributeType.TIMESTAMP).blockingGet(),
        attributeTypeConverter.convert(AttributeModelType.TIMESTAMP).blockingGet());
    assertEquals(Single.just(AttributeType.STRING_MAP).blockingGet(),
        attributeTypeConverter.convert(AttributeModelType.STRING_MAP).blockingGet());
    assertEquals(Single.just(AttributeType.STRING_ARRAY).blockingGet(),
        attributeTypeConverter.convert(AttributeModelType.STRING_ARRAY).blockingGet());
  }
}
