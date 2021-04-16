package org.hypertrace.core.graphql.log.event.schema;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.log.event.fetcher.LogEventFetcher;

public interface LogEventSchema {
  String LOG_EVENTS_QUERY_NAME = "";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(LOG_EVENTS_QUERY_NAME)
  @GraphQLDataFetcher(LogEventFetcher.class)
  LogEventResultSet logEvents(
      @GraphQLName(FilterArgument.ARGUMENT_NAME) List<FilterArgument> filterBy);
}
