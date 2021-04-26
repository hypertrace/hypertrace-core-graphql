package org.hypertrace.core.graphql.span.request;

import static io.reactivex.rxjava3.core.Single.zip;

import graphql.schema.DataFetchingFieldSelectionSet;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.log.event.request.LogEventRequest;

public class DefaultSpanRequestBuilder implements SpanRequestBuilder {

  private final ResultSetRequestBuilder resultSetRequestBuilder;
  private final LogEventAttributeRequestBuilder logEventAttributeRequestBuilder;

  public DefaultSpanRequestBuilder(
      ResultSetRequestBuilder resultSetRequestBuilder,
      LogEventAttributeRequestBuilder logEventAttributeRequestBuilder) {
    this.resultSetRequestBuilder = resultSetRequestBuilder;
    this.logEventAttributeRequestBuilder = logEventAttributeRequestBuilder;
  }

  @Override
  public Single<SpanRequest<OrderArgument>> build(
      GraphQlRequestContext context,
      String requestScope,
      Map<String, Object> arguments,
      DataFetchingFieldSelectionSet selectionSet) {
    return zip(
            resultSetRequestBuilder.build(
                context, requestScope, arguments, selectionSet, OrderArgument.class),
            logEventAttributeRequestBuilder.buildAttributeRequest(context, selectionSet),
            (resultSetRequest, logEventAttributeRequest) ->
                Single.just(new DefaultSpanRequest<>(resultSetRequest, logEventAttributeRequest)))
        .flatMap(single -> single);
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultSpanRequest<O extends OrderArgument> implements SpanRequest<O> {
    ResultSetRequest<O> spanEventsRequest;
    Collection<AttributeRequest> logEventAttributes;
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultResultSetRequest<O extends OrderArgument>
      implements ResultSetRequest<O> {
    GraphQlRequestContext context;
    Collection<AttributeRequest> attributes;
    AttributeRequest idAttribute;
    TimeRangeArgument timeRange;
    int limit;
    int offset;
    List<AttributeAssociation<O>> orderArguments;
    Collection<AttributeAssociation<FilterArgument>> filterArguments;
    Optional<String> spaceId;
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultLogEventRequest implements LogEventRequest {
    GraphQlRequestContext context;
    Collection<AttributeRequest> attributes;
    TimeRangeArgument timeRange;
    int limit;
    int offset;
    List<AttributeAssociation<OrderArgument>> orderArguments;
    Collection<AttributeAssociation<FilterArgument>> filterArguments;
  }
}
