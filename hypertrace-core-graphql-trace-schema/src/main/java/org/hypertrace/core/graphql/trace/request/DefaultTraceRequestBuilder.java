package org.hypertrace.core.graphql.trace.request;

import graphql.schema.DataFetchingFieldSelectionSet;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeModelScope;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.trace.schema.arguments.TraceType;
import org.hypertrace.core.graphql.trace.schema.arguments.TraceTypeArgument;

class DefaultTraceRequestBuilder implements TraceRequestBuilder {

  private final ResultSetRequestBuilder resultSetRequestBuilder;
  private final ArgumentDeserializer argumentDeserializer;
  private final TraceTypeToAttributeModelScopeConverter scopeConverter;

  @Inject
  DefaultTraceRequestBuilder(
      ResultSetRequestBuilder resultSetRequestBuilder,
      ArgumentDeserializer argumentDeserializer,
      TraceTypeToAttributeModelScopeConverter scopeConverter) {
    this.resultSetRequestBuilder = resultSetRequestBuilder;
    this.argumentDeserializer = argumentDeserializer;
    this.scopeConverter = scopeConverter;
  }

  @Override
  public Single<TraceRequest> build(
      GraphQlRequestContext context,
      Map<String, Object> arguments,
      DataFetchingFieldSelectionSet selectionSet) {

    TraceType traceType =
        this.argumentDeserializer
            .deserializePrimitive(arguments, TraceTypeArgument.class)
            .orElseThrow();

    return this.scopeConverter
        .convert(traceType)
        .flatMap(scope -> this.build(context, traceType, scope, arguments, selectionSet));
  }

  private Single<TraceRequest> build(
      GraphQlRequestContext context,
      TraceType traceType,
      AttributeModelScope scope,
      Map<String, Object> arguments,
      DataFetchingFieldSelectionSet selectionSet) {

    return this.resultSetRequestBuilder
        .build(context, scope, arguments, selectionSet)
        .map(resultSetRequest -> new DefaultTraceRequest(context, resultSetRequest, traceType));
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultTraceRequest implements TraceRequest {
    GraphQlRequestContext context;
    ResultSetRequest<OrderArgument> resultSetRequest;
    TraceType traceType;
  }
}
