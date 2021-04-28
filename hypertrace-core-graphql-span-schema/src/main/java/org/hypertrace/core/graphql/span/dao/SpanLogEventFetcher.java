package org.hypertrace.core.graphql.span.dao;

import static io.reactivex.rxjava3.core.Single.zip;
import static java.util.concurrent.TimeUnit.SECONDS;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeStore;
import org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterOperatorType;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterType;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.core.graphql.utils.grpc.GraphQlGrpcContextBuilder;
import org.hypertrace.gateway.service.GatewayServiceGrpc.GatewayServiceFutureStub;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.log.events.LogEventsRequest;
import org.hypertrace.gateway.service.v1.log.events.LogEventsResponse;
import org.hypertrace.gateway.service.v1.span.SpansResponse;

class SpanLogEventFetcher {

  private static final int DEFAULT_DEADLINE_SEC = 10;

  private final Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter;
  private final Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter;
  private final BiConverter<Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>
      attributeMapConverter;
  private final FilterRequestBuilder filterRequestBuilder;
  private final GatewayServiceFutureStub gatewayServiceStub;
  private final GraphQlGrpcContextBuilder grpcContextBuilder;
  private final AttributeStore attributeStore;

  @Inject
  SpanLogEventFetcher(
      Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter,
      Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter,
      BiConverter<Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>
          attributeMapConverter,
      FilterRequestBuilder filterRequestBuilder,
      GatewayServiceFutureStub gatewayServiceFutureStub,
      GraphQlGrpcContextBuilder grpcContextBuilder,
      AttributeStore attributeStore) {
    this.attributeConverter = attributeConverter;
    this.filterConverter = filterConverter;
    this.attributeMapConverter = attributeMapConverter;
    this.filterRequestBuilder = filterRequestBuilder;
    this.gatewayServiceStub = gatewayServiceFutureStub;
    this.grpcContextBuilder = grpcContextBuilder;
    this.attributeStore = attributeStore;
  }

  /**
   *
   *
   * <ul>
   *   <li>1. Fetch log event attributes from {@code gqlRequest}
   *   <li>2. Build log event request using attribute and spanIds as filter
   *   <li>3. Query log events
   *   <li>4. Processed log events response to build mapping from spanId to logEvent
   * </ul>
   */
  Single<SpanLogEventsResponse> fetchLogEvents(
      SpanRequest gqlRequest, SpansResponse spansResponse) {
    if (null == gqlRequest.spanEventsRequest().idAttribute()
        || null == gqlRequest.logEventAttributes()
        || gqlRequest.logEventAttributes().isEmpty()) {
      return Single.just(new SpanLogEventsResponse(spansResponse, Map.of()));
    }
    return buildLogEventsRequest(gqlRequest, spansResponse)
        .flatMap(
            logEventsRequest ->
                makeRequest(gqlRequest.spanEventsRequest().context(), logEventsRequest))
        .flatMap(
            logEventsResponse ->
                buildResponse(
                    gqlRequest.spanEventsRequest().context(),
                    gqlRequest.logEventAttributes(),
                    spansResponse,
                    logEventsResponse));
  }

  private Single<LogEventsRequest> buildLogEventsRequest(
      SpanRequest gqlRequest, SpansResponse spansResponse) {
    return zip(
        this.attributeConverter.convert(gqlRequest.logEventAttributes()),
        buildLogEventsQueryFilter(gqlRequest, spansResponse).flatMap(filterConverter::convert),
        (selections, filter) ->
            LogEventsRequest.newBuilder()
                .setStartTimeMillis(
                    gqlRequest.spanEventsRequest().timeRange().startTime().toEpochMilli())
                .setEndTimeMillis(
                    gqlRequest.spanEventsRequest().timeRange().endTime().toEpochMilli())
                .addAllSelection(selections)
                .setFilter(filter)
                .build());
  }

  private Single<List<AttributeAssociation<FilterArgument>>> buildLogEventsQueryFilter(
      SpanRequest gqlRequest, SpansResponse spansResponse) {
    List<String> spanIds =
        spansResponse.getSpansList().stream()
            .map(
                spanEvent ->
                    spanEvent
                        .getAttributesMap()
                        .get(gqlRequest.spanEventsRequest().idAttribute().attribute().id())
                        .getString())
            .collect(Collectors.toList());

    return filterRequestBuilder.build(
        gqlRequest.spanEventsRequest().context(),
        HypertraceCoreAttributeScopeString.LOG_EVENT,
        Set.of(new LogEventFilter(spanIds)));
  }

  private Single<LogEventsResponse> makeRequest(
      GraphQlRequestContext context, LogEventsRequest request) {
    return Single.fromFuture(
        this.grpcContextBuilder
            .build(context)
            .callInContext(
                () ->
                    this.gatewayServiceStub
                        .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                        .getLogEvents(request)));
  }

  private Single<SpanLogEventsResponse> buildResponse(
      GraphQlRequestContext graphQlRequestContext,
      Collection<AttributeRequest> attributeRequests,
      SpansResponse spansResponse,
      LogEventsResponse logEventsResponse) {
    String key =
        attributeStore
            .getForeignIdAttribute(
                graphQlRequestContext,
                HypertraceCoreAttributeScopeString.LOG_EVENT,
                HypertraceCoreAttributeScopeString.SPAN)
            .blockingGet()
            .key();
    return Observable.fromIterable(logEventsResponse.getLogEventsList())
        .concatMapSingle(
            logEventsResponseVar -> this.convert(attributeRequests, logEventsResponseVar))
        .collect(Collectors.groupingBy(logEvent -> (String) logEvent.attribute(key)))
        .map(
            spanIdVsLogEventsMap -> new SpanLogEventsResponse(spansResponse, spanIdVsLogEventsMap));
  }

  private Single<org.hypertrace.core.graphql.log.event.schema.LogEvent> convert(
      Collection<AttributeRequest> request,
      org.hypertrace.gateway.service.v1.log.events.LogEvent logEvent) {
    return this.attributeMapConverter
        .convert(request, logEvent.getAttributesMap())
        .map(ConvertedLogEvent::new);
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ConvertedLogEvent
      implements org.hypertrace.core.graphql.log.event.schema.LogEvent {

    Map<String, Object> attributeValues;

    @Override
    public Object attribute(String key) {
      return this.attributeValues.get(key);
    }
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class LogEventFilter implements FilterArgument {
    FilterType type = FilterType.ID;
    String key = null;
    FilterOperatorType operator = FilterOperatorType.IN;
    Collection<String> value;
    AttributeScope idType = null;
    String idScope = HypertraceCoreAttributeScopeString.SPAN;
  }
}
