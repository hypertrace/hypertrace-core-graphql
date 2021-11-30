package org.hypertrace.core.graphql.common.schema.attributes.arguments;

import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@GraphQLName(AttributeExpression.TYPE_NAME)
public class AttributeExpression {
  public static final String ARGUMENT_NAME = "expression";
  static final String TYPE_NAME = "AttributeExpression";

  private static final String ATTRIBUTE_KEY = "key";
  private static final String SUBPATH = "subpath";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(ATTRIBUTE_KEY)
  @JsonProperty(ATTRIBUTE_KEY)
  String key;

  //  @GraphQLField
  //  @GraphQLName(SUBPATH)
  //  @JsonProperty(SUBPATH)
  Optional<String> subpath;

  public String asAlias() {
    return subpath()
        .map(subpath -> String.format("%s.%s", this.key(), subpath))
        .orElseGet(this::key);
  }

  public static AttributeExpression forAttributeKey(@Nonnull String key) {
    return new AttributeExpression(key, Optional.empty());
  }
}
