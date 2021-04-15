package org.hypertrace.core.graphql.log.event.dao;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.log.event.schema.LogEventResultSet;
import org.hypertrace.gateway.service.v1.log.events.LogEventResponse;


public class GatewayServiceLogEventConverter {

  public Single<LogEventResultSet> convert(ResultSetRequest<?> request, LogEventResponse response) {

  }
}