package org.hypertrace.core.graphql.log.event.schema;

import graphql.annotations.annotationTypes.GraphQLName;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeQueryable;
import org.hypertrace.core.graphql.common.schema.id.Identifiable;

@GraphQLName(LogEvent.TYPE_NAME)
public interface LogEvent extends AttributeQueryable, Identifiable {
  String TYPE_NAME = "LogEvent";
}

