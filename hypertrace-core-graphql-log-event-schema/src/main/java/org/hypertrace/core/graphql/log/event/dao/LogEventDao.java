package org.hypertrace.core.graphql.log.event.dao;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.log.event.request.LogEventRequest;
import org.hypertrace.core.graphql.log.event.schema.LogEventResultSet;
import org.hypertrace.gateway.service.v1.log.events.LogEventsRequest;

public interface LogEventDao {

  Single<LogEventResultSet> getLogEvents(LogEventRequest request);
}
