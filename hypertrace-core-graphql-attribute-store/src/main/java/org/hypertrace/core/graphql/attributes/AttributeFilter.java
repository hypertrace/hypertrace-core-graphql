package org.hypertrace.core.graphql.attributes;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder
@Accessors(fluent = true)
public class AttributeFilter {
  String scope;
  String key;
}
