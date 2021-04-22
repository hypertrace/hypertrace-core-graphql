package org.hypertrace.core.graphql.utils.gateway;

import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.gateway.service.v1.common.Value;

class ValueWrapper {
  private final Value value;
  private final AttributeRequest attributeRequest;

  public ValueWrapper(Value value, AttributeRequest attributeRequest) {
    this.value = value;
    this.attributeRequest = attributeRequest;
  }

  public Value getValue() {
    return value;
  }

  public AttributeRequest getAttributeRequest() {
    return attributeRequest;
  }
}
