package org.hypertrace.core.graphql.attributes;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder
@Accessors(fluent = true)
public class AttributeUpdate {
  @Nullable String displayName;
}
