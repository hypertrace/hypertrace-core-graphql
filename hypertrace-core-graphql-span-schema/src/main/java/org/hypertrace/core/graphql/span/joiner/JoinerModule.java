package org.hypertrace.core.graphql.span.joiner;

import com.google.inject.AbstractModule;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.span.dao.SpanDao;
import org.hypertrace.core.graphql.span.joiner.multiple.span.DefaultMultipleSpanJoinerBuilder;
import org.hypertrace.core.graphql.span.joiner.multiple.span.MultipleSpanJoinerBuilder;
import org.hypertrace.core.graphql.span.joiner.single.span.DefaultSpanJoinerBuilder;
import org.hypertrace.core.graphql.span.joiner.single.span.SpanJoinerBuilder;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;

public class JoinerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SpanJoinerBuilder.class).to(DefaultSpanJoinerBuilder.class);
    bind(MultipleSpanJoinerBuilder.class).to(DefaultMultipleSpanJoinerBuilder.class);

    requireBinding(SpanDao.class);
    requireBinding(GraphQlSelectionFinder.class);
    requireBinding(ResultSetRequestBuilder.class);
    requireBinding(FilterRequestBuilder.class);
  }
}
