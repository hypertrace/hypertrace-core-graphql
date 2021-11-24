package org.hypertrace.core.graphql.utils.gateway;

import io.reactivex.rxjava3.core.Single;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.schema.attributes.arguments.AttributeExpression;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Expression.Builder;

class ColumnIdentifierExpressionConverter
    implements Converter<AttributeAssociation<AttributeExpression>, Expression> {

  private final ColumnIdentifierConverter columnIdentifierConverter;

  @Inject
  ColumnIdentifierExpressionConverter(ColumnIdentifierConverter columnIdentifierConverter) {
    this.columnIdentifierConverter = columnIdentifierConverter;
  }

  @Override
  public Single<Expression> convert(AttributeAssociation<AttributeExpression> attributeExpression) {
    // TODO: AttributeExpression support
    return this.columnIdentifierConverter
        .convert(attributeExpression.attribute())
        .map(Expression.newBuilder()::setColumnIdentifier)
        .map(Builder::build);
  }
}
