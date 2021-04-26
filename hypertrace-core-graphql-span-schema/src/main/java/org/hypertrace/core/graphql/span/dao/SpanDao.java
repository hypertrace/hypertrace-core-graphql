package org.hypertrace.core.graphql.span.dao;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.core.graphql.span.schema.SpanResultSet;

public interface SpanDao {

  Single<SpanResultSet> getSpans(SpanRequest<OrderArgument> request);
}
