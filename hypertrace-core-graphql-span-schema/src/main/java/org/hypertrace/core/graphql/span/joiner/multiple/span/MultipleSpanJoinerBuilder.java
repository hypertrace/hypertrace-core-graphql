package org.hypertrace.core.graphql.span.joiner.multiple.span;

import graphql.schema.DataFetchingFieldSelectionSet;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

public interface MultipleSpanJoinerBuilder {
  Single<MultipleSpanJoiner> build(
      GraphQlRequestContext context,
      TimeRangeArgument timeRange,
      DataFetchingFieldSelectionSet selectionSet,
      List<String> pathToMultipleSpanJoin);
}
