package org.hypertrace.core.graphql.span.joiner;

import static org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString.SPAN;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;
import org.hypertrace.core.graphql.common.schema.attributes.arguments.AttributeExpression;
import org.hypertrace.core.graphql.common.schema.id.Identifiable;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterOperatorType;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterType;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.span.dao.SpanDao;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.core.graphql.span.schema.Span;
import org.hypertrace.core.graphql.span.schema.SpanResultSet;

@AllArgsConstructor(onConstructor_ = {@Inject})
public class SourceToSpansProvider {

  private static final int ZERO_OFFSET = 0;

  private final SpanDao spanDao;
  private final ResultSetRequestBuilder resultSetRequestBuilder;
  private final FilterRequestBuilder filterRequestBuilder;

  public <T> Single<ListMultimap<T, Span>> joinSpans(
      GraphQlRequestContext context,
      TimeRangeArgument timeRange,
      List<SelectedField> selectedFields,
      Map<T, List<String>> sourceToSpanIdsMap) {
    return this.buildSpanRequest(context, timeRange, selectedFields, sourceToSpanIdsMap)
        .flatMap(spanDao::getSpans)
        .map(this::buildSpanIdToSpanMap)
        .map(spanIdToSpanMap -> buildSourceToSpanListMultiMap(sourceToSpanIdsMap, spanIdToSpanMap));
  }

  private <T> ListMultimap<T, Span> buildSourceToSpanListMultiMap(
      Map<T, List<String>> sourceToSpanIdsMap, Map<String, Span> spanIdToSpanMap) {
    ListMultimap<T, Span> listMultimap = ArrayListMultimap.create();
    for (Entry<T, List<String>> entry : sourceToSpanIdsMap.entrySet()) {
      T source = entry.getKey();
      for (String spanId : entry.getValue()) {
        if (spanIdToSpanMap.containsKey(spanId)) {
          listMultimap.put(source, spanIdToSpanMap.get(spanId));
        }
      }
    }
    return Multimaps.unmodifiableListMultimap(listMultimap);
  }

  private Map<String, Span> buildSpanIdToSpanMap(SpanResultSet resultSet) {
    return resultSet.results().stream()
        .collect(Collectors.toUnmodifiableMap(Identifiable::id, Function.identity()));
  }

  private <T> Single<SpanRequest> buildSpanRequest(
      GraphQlRequestContext context,
      TimeRangeArgument timeRange,
      List<SelectedField> selectedFields,
      Map<T, List<String>> sourceToSpanIdsMap) {
    Collection<String> spanIds =
        sourceToSpanIdsMap.values().stream()
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toUnmodifiableList());
    return buildSpanIdsFilter(context, spanIds)
        .flatMap(
            filterArguments ->
                buildSpanRequest(
                    spanIds.size(), context, timeRange, selectedFields, filterArguments));
  }

  private Single<SpanRequest> buildSpanRequest(
      int size,
      GraphQlRequestContext context,
      TimeRangeArgument timeRange,
      List<SelectedField> selectedFields,
      List<AttributeAssociation<FilterArgument>> filterArguments) {
    return resultSetRequestBuilder
        .build(
            context,
            SPAN,
            size,
            ZERO_OFFSET,
            timeRange,
            Collections.emptyList(),
            filterArguments,
            selectedFields.stream(),
            Optional.empty())
        .map(spanEventsRequest -> new SpanJoinRequest(context, spanEventsRequest));
  }

  private Single<List<AttributeAssociation<FilterArgument>>> buildSpanIdsFilter(
      GraphQlRequestContext context, Collection<String> spanIds) {
    return filterRequestBuilder.build(context, SPAN, Set.of(new SpanIdFilter(spanIds)));
  }

  @Value
  @Accessors(fluent = true)
  private static class SpanIdFilter implements FilterArgument {
    FilterType type = FilterType.ID;
    String key = null;
    AttributeExpression keyExpression = null;
    FilterOperatorType operator = FilterOperatorType.IN;
    Collection<String> value;
    AttributeScope idType = null;
    String idScope = SPAN;
  }

  @Value
  @Accessors(fluent = true)
  private static class SpanJoinRequest implements SpanRequest {
    GraphQlRequestContext context;
    ResultSetRequest<OrderArgument> spanEventsRequest;
    Collection<AttributeRequest> logEventAttributes = Collections.emptyList();
    boolean fetchTotal = false;
  }
}
