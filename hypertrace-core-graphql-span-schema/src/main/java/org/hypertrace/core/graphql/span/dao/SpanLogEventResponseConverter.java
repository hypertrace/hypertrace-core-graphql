package org.hypertrace.core.graphql.span.dao;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeStore;
import org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.schema.attributes.arguments.AttributeExpression;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.log.event.schema.LogEvent;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.log.events.LogEventsResponse;
import org.hypertrace.gateway.service.v1.span.SpansResponse;

class SpanLogEventResponseConverter {

  private final BiConverter<Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>
      attributeMapConverter;
  private final AttributeStore attributeStore;

  @Inject
  SpanLogEventResponseConverter(
      BiConverter<Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>
          attributeMapConverter,
      AttributeStore attributeStore) {
    this.attributeMapConverter = attributeMapConverter;
    this.attributeStore = attributeStore;
  }

  Single<SpanLogEventsResponse> buildResponse(
      GraphQlRequestContext graphQlRequestContext,
      Collection<AttributeRequest> attributeRequests,
      SpansResponse spansResponse,
      LogEventsResponse logEventsResponse) {
    return this.attributeStore
        .getForeignIdAttribute(
            graphQlRequestContext,
            HypertraceCoreAttributeScopeString.LOG_EVENT,
            HypertraceCoreAttributeScopeString.SPAN)
        .flatMap(
            spanId -> buildResponse(spanId, attributeRequests, spansResponse, logEventsResponse));
  }

  private Single<SpanLogEventsResponse> buildResponse(
      AttributeModel foreignIdAttribute,
      Collection<AttributeRequest> attributeRequests,
      SpansResponse spansResponse,
      LogEventsResponse logEventsResponse) {
    return Observable.fromIterable(logEventsResponse.getLogEventsList())
        .concatMapSingle(
            logEventsResponseVar ->
                this.convert(foreignIdAttribute, attributeRequests, logEventsResponseVar))
        .collect(
            Collectors.groupingBy(
                SpanLogEventPair::spanId,
                Collectors.mapping(SpanLogEventPair::logEvent, Collectors.toList())))
        .map(
            spanIdVsLogEventsMap -> new SpanLogEventsResponse(spansResponse, spanIdVsLogEventsMap));
  }

  private Single<SpanLogEventPair> convert(
      AttributeModel foreignIdAttribute,
      Collection<AttributeRequest> request,
      org.hypertrace.gateway.service.v1.log.events.LogEvent logEvent) {
    return this.attributeMapConverter
        .convert(request, logEvent.getAttributesMap())
        .map(
            attributeMap ->
                new SpanLogEventPair(
                    logEvent.getAttributesMap().get(foreignIdAttribute.id()).getString(),
                    new ConvertedLogEvent(attributeMap)));
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class SpanLogEventPair {
    String spanId;
    LogEvent logEvent;
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ConvertedLogEvent
      implements org.hypertrace.core.graphql.log.event.schema.LogEvent {

    Map<String, Object> attributeValues;

    @Override
    public Object attribute(AttributeExpression attributeExpression) {
      return this.attributeValues.get(attributeExpression.asMapKey());
    }
  }
}
