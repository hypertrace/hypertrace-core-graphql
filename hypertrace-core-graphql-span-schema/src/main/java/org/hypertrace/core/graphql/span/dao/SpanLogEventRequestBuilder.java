package org.hypertrace.core.graphql.span.dao;

import static io.reactivex.rxjava3.core.Single.zip;

import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeStore;
import org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterOperatorType;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterType;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.log.events.LogEventsRequest;
import org.hypertrace.gateway.service.v1.span.SpansResponse;

class SpanLogEventRequestBuilder {

  private final Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter;
  private final Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter;
  private final FilterRequestBuilder filterRequestBuilder;
  private final AttributeStore attributeStore;

  @Inject
  SpanLogEventRequestBuilder(
      Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter,
      Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter,
      FilterRequestBuilder filterRequestBuilder,
      AttributeStore attributeStore) {
    this.attributeConverter = attributeConverter;
    this.filterConverter = filterConverter;
    this.filterRequestBuilder = filterRequestBuilder;
    this.attributeStore = attributeStore;
  }

  Single<LogEventsRequest> buildLogEventsRequest(
      SpanRequest gqlRequest, SpansResponse spansResponse) {
    return getRequestAttributes(
            gqlRequest.spanEventsRequest().context(), gqlRequest.logEventAttributes())
        .flatMap(
            v ->
                zip(
                    this.attributeConverter.convert(v),
                    buildLogEventsQueryFilter(gqlRequest, spansResponse)
                        .flatMap(filterConverter::convert),
                    (selections, filter) ->
                        LogEventsRequest.newBuilder()
                            .setStartTimeMillis(
                                gqlRequest
                                    .spanEventsRequest()
                                    .timeRange()
                                    .startTime()
                                    .toEpochMilli())
                            .setEndTimeMillis(
                                gqlRequest.spanEventsRequest().timeRange().endTime().toEpochMilli())
                            .addAllSelection(selections)
                            .setFilter(filter)
                            .build()));
  }

  private Single<Collection<AttributeRequest>> getRequestAttributes(
      GraphQlRequestContext requestContext, Collection<AttributeRequest> logEventAttributes) {
    return this.attributeStore
        .getForeignIdAttribute(
            requestContext,
            HypertraceCoreAttributeScopeString.LOG_EVENT,
            HypertraceCoreAttributeScopeString.SPAN)
        .flatMap(
            spanId ->
                logEventAttributes.stream()
                        .anyMatch(
                            logEventAttribute ->
                                logEventAttribute.attribute().key().equals(spanId.key()))
                    ? Single.just(logEventAttributes)
                    : updateRequestAttribute(spanId, logEventAttributes));
  }

  private Single<Collection<AttributeRequest>> updateRequestAttribute(
      AttributeModel attributeModel, Collection<AttributeRequest> requests) {
    List<AttributeRequest> list = new ArrayList<>(requests);
    list.add(new DefaultAttributeRequest(attributeModel, ""));
    return Single.just(list);
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

  @lombok.Value
  @Accessors(fluent = true)
  class DefaultAttributeRequest implements AttributeRequest {
    AttributeModel attribute;
    String alias;
  }
}
