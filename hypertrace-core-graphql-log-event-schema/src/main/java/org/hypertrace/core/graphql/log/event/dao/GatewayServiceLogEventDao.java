package org.hypertrace.core.graphql.log.event.dao;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.log.event.schema.LogEventResultSet;

public class GatewayServiceLogEventDao implements LogEventDao {

  @Override
  public Single<LogEventResultSet> getLogEvents(ResultSetRequest<OrderArgument> request) {
    return null;
  }
}
