package org.hypertrace.core.graphql.common.schema.results.arguments.filter;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface LogicalFilterOperatorArgument extends PrimitiveArgument<LogicalFilterOperator> {

  String ARGUMENT_NAME = "logicalOperator";
}
