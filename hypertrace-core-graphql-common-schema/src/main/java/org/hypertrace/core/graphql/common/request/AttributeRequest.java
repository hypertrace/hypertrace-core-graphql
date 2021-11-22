package org.hypertrace.core.graphql.common.request;

import java.util.Optional;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.common.schema.attributes.arguments.AttributeExpression;

public interface AttributeRequest {

  AttributeModel attribute();

  String alias();

  Optional<String> subpath();

  default String asMapKey() {
    return new AttributeExpression(attribute().key(), subpath()).asMapKey();
  }
}
