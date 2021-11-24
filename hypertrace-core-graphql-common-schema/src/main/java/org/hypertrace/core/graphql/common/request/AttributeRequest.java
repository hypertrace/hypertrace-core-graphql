package org.hypertrace.core.graphql.common.request;

import org.hypertrace.core.graphql.common.schema.attributes.arguments.AttributeExpression;

public interface AttributeRequest {

  AttributeAssociation<AttributeExpression> attributeExpression();

  String alias();

  default String asMapKey() {
    return attributeExpression().value().asMapKey();
  }
}
