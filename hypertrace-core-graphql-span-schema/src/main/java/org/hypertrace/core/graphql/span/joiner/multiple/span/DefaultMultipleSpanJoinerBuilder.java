package org.hypertrace.core.graphql.span.joiner.multiple.span;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.concat;
import static org.hypertrace.core.graphql.span.joiner.multiple.span.MultipleSpanJoin.SPANS_KEY;

import com.google.common.collect.ListMultimap;
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

public class DefaultMultipleSpanJoinerBuilder implements MultipleSpanJoinerBuilder {

  private final GraphQlSelectionFinder selectionFinder;

  private final SourceToSpansProvider sourceToSpansProvider;

  @Inject
  DefaultMultipleSpanJoinerBuilder(
      GraphQlSelectionFinder selectionFinder, SourceToSpansProvider sourceToSpansProvider) {
    this.selectionFinder = selectionFinder;
    this.sourceToSpansProvider = sourceToSpansProvider;
  }

  @Override
  public Single<MultipleSpanJoiner> build(
      GraphQlRequestContext context,
      TimeRangeArgument timeRange,
      DataFetchingFieldSelectionSet selectionSet,
      List<String> pathToSpanJoin) {
    return Single.just(
        new DefaultMultipleSpanJoiner(
            context, timeRange, this.getSelections(selectionSet, pathToSpanJoin)));
  }

  private List<SelectedField> getSelections(
      DataFetchingFieldSelectionSet selectionSet, List<String> pathToSpanJoin) {
    List<String> fullPath = copyOf(concat(pathToSpanJoin, List.of(SPANS_KEY)));
    return selectionFinder
        .findSelections(selectionSet, SelectionQuery.builder().selectionPath(fullPath).build())
        .collect(Collectors.toUnmodifiableList());
  }

  @AllArgsConstructor
  private class DefaultMultipleSpanJoiner implements MultipleSpanJoiner {

    private final GraphQlRequestContext context;
    private final TimeRangeArgument timeRange;
    private final List<SelectedField> selectedFields;

    @Override
    public <T> Single<ListMultimap<T, Span>> joinMultipleSpans(
        Collection<T> joinSources, MultipleSpanIdGetter<T> multipleSpanIdGetter) {
      return this.buildSourceToIdsMap(joinSources, multipleSpanIdGetter)
          .flatMap(
              sourceToSpanIdsMap ->
                  sourceToSpansProvider.joinSpans(
                      context, timeRange, selectedFields, sourceToSpanIdsMap));
    }

    private <T> Single<Map<T, List<String>>> buildSourceToIdsMap(
        Collection<T> joinSources, MultipleSpanIdGetter<T> multipleSpanIdGetter) {
      return Observable.fromIterable(joinSources)
          .flatMapSingle(source -> this.maybeBuildMapEntry(source, multipleSpanIdGetter))
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private <T> Single<Entry<T, List<String>>> maybeBuildMapEntry(
        T source, MultipleSpanIdGetter<T> multipleSpanIdGetter) {
      return multipleSpanIdGetter.getSpanIds(source).map(ids -> Map.entry(source, ids));
    }
  }
}
