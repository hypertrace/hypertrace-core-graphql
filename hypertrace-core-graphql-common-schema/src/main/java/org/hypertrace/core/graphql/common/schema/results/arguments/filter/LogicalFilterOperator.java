package org.hypertrace.core.graphql.common.schema.results.arguments.filter;

import graphql.annotations.annotationTypes.GraphQLName;

@GraphQLName(LogicalFilterOperator.TYPE_NAME)
public enum LogicalFilterOperator {
  AND,
  OR;

  static final String TYPE_NAME = "LogicalFilterOperator";
}
