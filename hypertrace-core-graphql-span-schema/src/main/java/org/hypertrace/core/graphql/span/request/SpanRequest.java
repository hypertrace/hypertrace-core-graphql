package org.hypertrace.core.graphql.span.request;

import java.util.Collection;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;

public interface SpanRequest<O extends OrderArgument> {
  ResultSetRequest<O> spanEventsRequest();

  Collection<AttributeRequest> logEventAttributes();
}
