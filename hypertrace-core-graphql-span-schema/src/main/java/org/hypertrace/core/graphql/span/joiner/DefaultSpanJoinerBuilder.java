package org.hypertrace.core.graphql.span.joiner;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.concat;
import static org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString.SPAN;
import static org.hypertrace.core.graphql.span.joiner.SpanJoin.SPANS_KEY;
import static org.hypertrace.core.graphql.span.joiner.SpanJoin.SPAN_KEY;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Observable;
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
import javax.inject.Inject;
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
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;

public class DefaultSpanJoinerBuilder implements SpanJoinerBuilder {

  private static final int ZERO_OFFSET = 0;
  private final SpanDao spanDao;
  private final GraphQlSelectionFinder selectionFinder;

  private final ResultSetRequestBuilder resultSetRequestBuilder;
  private final FilterRequestBuilder filterRequestBuilder;

  @Inject
  DefaultSpanJoinerBuilder(
      SpanDao spanDao,
      GraphQlSelectionFinder selectionFinder,
      ResultSetRequestBuilder resultSetRequestBuilder,
      FilterRequestBuilder filterRequestBuilder) {
    this.spanDao = spanDao;
    this.selectionFinder = selectionFinder;
    this.resultSetRequestBuilder = resultSetRequestBuilder;
    this.filterRequestBuilder = filterRequestBuilder;
  }

  @Override
  public Single<SpanJoiner> build(
      GraphQlRequestContext context,
      TimeRangeArgument timeRange,
      DataFetchingFieldSelectionSet selectionSet,
      List<String> pathToSpanJoin) {
    return Single.just(new DefaultSpanJoiner(context, timeRange, selectionSet, pathToSpanJoin));
  }

  @AllArgsConstructor
  private class DefaultSpanJoiner implements SpanJoiner {

    private final GraphQlRequestContext context;
    private final TimeRangeArgument timeRange;
    private final DataFetchingFieldSelectionSet selectionSet;
    private final List<String> pathToJoin;

    @Override
    public <T> Single<Map<T, Span>> joinSpan(
        Collection<T> joinSources, SpanIdGetter<T> spanIdGetter) {
      return this.buildSourceToIdMap(joinSources, spanIdGetter)
          .flatMap(
              sourceToSpanIdsMap ->
                  this.buildSpanRequest(sourceToSpanIdsMap, SPAN_KEY)
                      .flatMap(spanDao::getSpans)
                      .map(this::buildSpanIdToSpanMap)
                      .map(
                          spanIdToSpanMap ->
                              this.buildSourceToSpanListMultiMap(
                                  sourceToSpanIdsMap, spanIdToSpanMap)))
          .map(Multimaps::asMap)
          .map(this::reduceMap);
    }

    @Override
    public <T> Single<ListMultimap<T, Span>> joinSpans(
        Collection<T> joinSources, MultipleSpanIdGetter<T> multipleSpanIdGetter) {
      return this.buildSourceToIdsMap(joinSources, multipleSpanIdGetter)
          .flatMap(
              sourceToSpanIdsMap ->
                  this.buildSpanRequest(sourceToSpanIdsMap, SPANS_KEY)
                      .flatMap(spanDao::getSpans)
                      .map(this::buildSpanIdToSpanMap)
                      .map(
                          spanIdToSpanMap ->
                              this.buildSourceToSpanListMultiMap(
                                  sourceToSpanIdsMap, spanIdToSpanMap)));
    }

    private <T> Map<T, Span> reduceMap(Map<T, List<Span>> multiMap) {
      return multiMap.entrySet().stream()
          .filter(entry -> !entry.getValue().isEmpty())
          .collect(Collectors.toUnmodifiableMap(Entry::getKey, entry -> entry.getValue().get(0)));
    }

    private <T> Single<Map<T, List<String>>> buildSourceToIdMap(
        Collection<T> joinSources, SpanIdGetter<T> spanIdGetter) {
      return Observable.fromIterable(joinSources)
          .flatMapSingle(source -> this.maybeBuildMapEntry(source, spanIdGetter))
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private <T> Single<Map<T, List<String>>> buildSourceToIdsMap(
        Collection<T> joinSources, MultipleSpanIdGetter<T> multipleSpanIdGetter) {
      return Observable.fromIterable(joinSources)
          .flatMapSingle(source -> this.maybeBuildMapEntry(source, multipleSpanIdGetter))
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private <T> Single<Entry<T, List<String>>> maybeBuildMapEntry(
        T source, SpanIdGetter<T> spanIdGetter) {
      return spanIdGetter.getSpanId(source).map(List::of).map(ids -> Map.entry(source, ids));
    }

    private <T> Single<Entry<T, List<String>>> maybeBuildMapEntry(
        T source, MultipleSpanIdGetter<T> multipleSpanIdGetter) {
      return multipleSpanIdGetter.getSpanIds(source).map(ids -> Map.entry(source, ids));
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

    private List<SelectedField> getSelections(String joinSpanKey) {
      List<String> fullPath = copyOf(concat(pathToJoin, List.of(joinSpanKey)));
      return selectionFinder
          .findSelections(selectionSet, SelectionQuery.builder().selectionPath(fullPath).build())
          .collect(Collectors.toUnmodifiableList());
    }

    private Map<String, Span> buildSpanIdToSpanMap(SpanResultSet resultSet) {
      return resultSet.results().stream()
          .collect(Collectors.toUnmodifiableMap(Identifiable::id, Function.identity()));
    }

    private <T> Single<SpanRequest> buildSpanRequest(
        Map<T, List<String>> sourceToSpanIdsMap, String joinSpanKey) {
      Collection<String> spanIds =
          sourceToSpanIdsMap.values().stream()
              .flatMap(List::stream)
              .distinct()
              .collect(Collectors.toUnmodifiableList());
      List<SelectedField> selectedFields = getSelections(joinSpanKey);
      return buildSpanIdsFilter(context, spanIds)
          .flatMap(
              filterArguments -> buildSpanRequest(spanIds.size(), filterArguments, selectedFields));
    }

    private Single<SpanRequest> buildSpanRequest(
        int size,
        List<AttributeAssociation<FilterArgument>> filterArguments,
        List<SelectedField> selectedFields) {
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
