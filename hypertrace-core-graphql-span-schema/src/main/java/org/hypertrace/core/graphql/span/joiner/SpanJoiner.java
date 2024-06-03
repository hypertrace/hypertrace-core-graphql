package org.hypertrace.core.graphql.span.joiner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hypertrace.core.graphql.span.schema.Span;

public interface SpanJoiner {

  /** A NOOP joiner */
  SpanJoiner NO_OP_JOINER =
      new SpanJoiner() {
        @Override
        public <T> Single<Map<T, Span>> joinSpan(
            Collection<T> joinSources, SpanIdGetter<T> spanIdGetter, SpanSource spanSource) {
          return Single.just(Collections.emptyMap());
        }

        @Override
        public <T> Single<ListMultimap<T, Span>> joinSpans(
            Collection<T> joinSources,
            MultipleSpanIdGetter<T> multipleSpanIdGetter,
            SpanSource spanSource) {
          return Single.just(ArrayListMultimap.create());
        }
      };

  <T> Single<Map<T, Span>> joinSpan(
      Collection<T> joinSources, SpanIdGetter<T> spanIdGetter, SpanSource spanSource);

  <T> Single<ListMultimap<T, Span>> joinSpans(
      Collection<T> joinSources,
      MultipleSpanIdGetter<T> multipleSpanIdGetter,
      SpanSource spanSource);

  @FunctionalInterface
  interface SpanIdGetter<T> {
    Single<String> getSpanId(T source);
  }

  @FunctionalInterface
  interface MultipleSpanIdGetter<T> {
    Single<List<String>> getSpanIds(T source);
  }
}
