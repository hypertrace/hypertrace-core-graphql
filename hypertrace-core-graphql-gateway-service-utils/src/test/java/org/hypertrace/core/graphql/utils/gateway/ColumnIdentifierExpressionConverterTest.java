package org.hypertrace.core.graphql.utils.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.schema.attributes.arguments.AttributeExpression;
import org.hypertrace.gateway.service.v1.common.ColumnIdentifier;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ColumnIdentifierExpressionConverterTest {
  @Mock ColumnIdentifierConverter mockColumnIdentifierConverter;
  @Mock AttributeModel mockAttribute;

  @Test
  void convertsColumnBasedOnAttributeId() {
    ColumnIdentifier expectedColumnIdentifier =
        ColumnIdentifier.newBuilder().setColumnName("foo").build();
    ColumnIdentifierExpressionConverter columnIdentifierExpressionConverter =
        new ColumnIdentifierExpressionConverter(this.mockColumnIdentifierConverter);

    when(this.mockColumnIdentifierConverter.convert(eq(mockAttribute)))
        .thenReturn(Single.just(expectedColumnIdentifier));

    assertEquals(
        Expression.newBuilder().setColumnIdentifier(expectedColumnIdentifier).build(),
        columnIdentifierExpressionConverter
            .convert(
                AttributeAssociation.of(
                    this.mockAttribute, AttributeExpression.forAttributeKey("key")))
            .blockingGet());
  }
}
