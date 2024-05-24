package org.hypertrace.core.graphql.span.joiner.single.span;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.concat;
import static org.hypertrace.core.graphql.span.joiner.single.span.SpanJoin.SPAN_KEY;

import com.google.common.collect.Multimaps;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.span.joiner.SourceToSpansProvider;
import org.hypertrace.core.graphql.span.schema.Span;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;

public class DefaultSpanJoinerBuilder implements SpanJoinerBuilder {

  private final GraphQlSelectionFinder selectionFinder;

  private final SourceToSpansProvider sourceToSpansProvider;

  @Inject
  DefaultSpanJoinerBuilder(
      GraphQlSelectionFinder selectionFinder, SourceToSpansProvider sourceToSpansProvider) {
    this.selectionFinder = selectionFinder;
    this.sourceToSpansProvider = sourceToSpansProvider;
  }

  @Override
  public Single<SpanJoiner> build(
      GraphQlRequestContext context,
      TimeRangeArgument timeRange,
      DataFetchingFieldSelectionSet selectionSet,
      List<String> pathToSpanJoin) {
    return Single.just(
        new DefaultSpanJoiner(
            context, timeRange, this.getSelections(selectionSet, pathToSpanJoin)));
  }

  private List<SelectedField> getSelections(
      DataFetchingFieldSelectionSet selectionSet, List<String> pathToSpanJoin) {
    List<String> fullPath = copyOf(concat(pathToSpanJoin, List.of(SPAN_KEY)));
    return selectionFinder
        .findSelections(selectionSet, SelectionQuery.builder().selectionPath(fullPath).build())
        .collect(Collectors.toUnmodifiableList());
  }

  @AllArgsConstructor
  private class DefaultSpanJoiner implements SpanJoiner {

    private final GraphQlRequestContext context;
    private final TimeRangeArgument timeRange;
    private final List<SelectedField> selectedFields;

    @Override
    public <T> Single<Map<T, Span>> joinSpans(
        Collection<T> joinSources, SpanIdGetter<T> spanIdGetter) {
      return this.buildSourceToIdMap(joinSources, spanIdGetter)
          .flatMap(
              sourceToSpanIdsMap ->
                  sourceToSpansProvider.joinSpans(
                      context, timeRange, selectedFields, sourceToSpanIdsMap))
          .map(Multimaps::asMap)
          .map(this::reduceMap);
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

    private <T> Single<Entry<T, List<String>>> maybeBuildMapEntry(
        T source, SpanIdGetter<T> spanIdGetter) {
      return spanIdGetter.getSpanId(source).map(List::of).map(ids -> Map.entry(source, ids));
    }
  }
}
