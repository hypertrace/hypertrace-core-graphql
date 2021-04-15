package org.hypertrace.core.graphql.log.event.dao;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.gateway.service.v1.span.SpansRequest;

public class GatewayServiceLogEventRequestBuilder {

  Single<SpansRequest> buildRequest(ResultSetRequest<OrderArgument> gqlRequest) {
    return null;
  }
}
