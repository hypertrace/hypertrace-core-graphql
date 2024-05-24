package org.hypertrace.core.graphql.span.joiner.multiple.span;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import org.hypertrace.core.graphql.span.schema.Span;

public interface MultipleSpanJoiner {

  /** A NOOP joiner */
  MultipleSpanJoiner NO_OP_JOINER =
      new MultipleSpanJoiner() {
        @Override
        public <T> Single<ListMultimap<T, Span>> joinMultipleSpans(
            Collection<T> joinSources, MultipleSpanIdGetter<T> multipleSpanIdGetter) {
          return Single.just(ArrayListMultimap.create());
        }
      };

  <T> Single<ListMultimap<T, Span>> joinMultipleSpans(
      Collection<T> joinSources, MultipleSpanIdGetter<T> multipleSpanIdGetter);

  @FunctionalInterface
  interface MultipleSpanIdGetter<T> {
    Single<List<String>> getSpanIds(T source);
  }
}
