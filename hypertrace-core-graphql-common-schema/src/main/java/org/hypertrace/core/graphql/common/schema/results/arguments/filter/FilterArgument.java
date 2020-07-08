package org.hypertrace.core.graphql.common.schema.results.arguments.filter;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import javax.annotation.Nullable;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;

// TODO revisit union types
@GraphQLName(FilterArgument.TYPE_NAME)
public interface FilterArgument {
  String TYPE_NAME = "Filter";
  String ARGUMENT_NAME = "filterBy"; // TODO rename to filter
  String FILTER_ARGUMENT_TYPE = "type";
  String FILTER_ARGUMENT_KEY = "key";
  String FILTER_ARGUMENT_OPERATOR = "operator";
  String FILTER_ARGUMENT_VALUE = "value";
  String FILTER_ARGUMENT_ID_TYPE = "idType"; // TODO rename

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(FILTER_ARGUMENT_TYPE)
  FilterType type();

  @GraphQLField
  @GraphQLName(FILTER_ARGUMENT_KEY)
  @Nullable
  String key();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(FILTER_ARGUMENT_OPERATOR)
  FilterOperatorType operator();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(FILTER_ARGUMENT_VALUE)
  Object value();

  @GraphQLField
  @GraphQLName(FILTER_ARGUMENT_ID_TYPE)
  @Nullable
  AttributeScope idScope();
}
