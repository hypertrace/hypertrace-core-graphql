package org.hypertrace.core.graphql.span.joiner.multiple.span;

import static java.util.Collections.emptyList;
import static org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString.SPAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.attributes.arguments.AttributeExpression;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.log.event.schema.LogEventResultSet;
import org.hypertrace.core.graphql.span.dao.SpanDao;
import org.hypertrace.core.graphql.span.joiner.SourceToSpansProvider;
import org.hypertrace.core.graphql.span.joiner.multiple.span.MultipleSpanJoiner.MultipleSpanIdGetter;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.core.graphql.span.schema.Span;
import org.hypertrace.core.graphql.span.schema.SpanResultSet;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MultipleSpanJoinerBuilderTest {

  private static final String SPAN_ID1 = "spanId1";
  private static final String SPAN_ID2 = "spanId2";
  private static final String SPAN_ID3 = "spanId3";
  private static final String SPAN_ID4 = "spanId4";

  @Mock private SpanDao mockSpanDao;
  @Mock private GraphQlSelectionFinder mockSelectionFinder;
  @Mock private ResultSetRequestBuilder mockResultSetRequestBuilder;
  @Mock private FilterRequestBuilder mockFilterRequestBuilder;
  @Mock private DataFetchingFieldSelectionSet mockSelectionSet;
  @Mock private GraphQlRequestContext mockRequestContext;
  @Mock private ResultSetRequest<OrderArgument> mockResultSetRequest;
  @Mock private AttributeAssociation<FilterArgument> mockFilter;
  @Mock private TimeRangeArgument mockTimeRangeArgument;

  private MultipleSpanJoinerBuilder multipleSpanJoinerBuilder;

  @BeforeEach
  void setup() {
    multipleSpanJoinerBuilder =
        new DefaultMultipleSpanJoinerBuilder(
            mockSelectionFinder,
            new SourceToSpansProvider(
                mockSpanDao, mockResultSetRequestBuilder, mockFilterRequestBuilder));
  }

  @Test
  void fetchMultipleSpans() {
    Span span1 = new TestSpan(SPAN_ID1);
    Span span2 = new TestSpan(SPAN_ID2);
    TestJoinSource joinSource1 = new TestJoinSource(List.of(SPAN_ID1, SPAN_ID2));
    TestJoinSource joinSource2 = new TestJoinSource(List.of(SPAN_ID3, SPAN_ID4));
    ListMultimap<TestJoinSource, Span> expected = ArrayListMultimap.create();
    expected.put(joinSource1, span1);
    expected.put(joinSource1, span2);
    List<TestJoinSource> joinSources = List.of(joinSource1, joinSource2);
    mockRequestedSelectionFields(List.of(mock(SelectedField.class), mock(SelectedField.class)));
    mockRequestBuilding();
    mockResult(List.of(span1, span2));
    MultipleSpanJoiner joiner =
        this.multipleSpanJoinerBuilder
            .build(
                this.mockRequestContext,
                this.mockTimeRangeArgument,
                this.mockSelectionSet,
                List.of("pathToSpans"))
            .blockingGet();
    assertEquals(
        expected,
        joiner.joinMultipleSpans(joinSources, new TestJoinSourceIdGetter()).blockingGet());
  }

  private void mockRequestBuilding() {
    when(mockFilterRequestBuilder.build(eq(mockRequestContext), eq(SPAN), anySet()))
        .thenAnswer(invocation -> Single.just(List.of(mockFilter)));

    when(mockResultSetRequestBuilder.build(
            eq(mockRequestContext),
            eq(SPAN),
            eq(4),
            eq(0),
            eq(mockTimeRangeArgument),
            eq(emptyList()),
            eq(List.of(mockFilter)),
            any(Stream.class),
            eq(Optional.empty())))
        .thenAnswer(invocation -> Single.just(mockResultSetRequest));
  }

  private void mockRequestedSelectionFields(List<SelectedField> selectedFields) {
    when(mockSelectionFinder.findSelections(
            mockSelectionSet,
            SelectionQuery.builder().selectionPath(List.of("pathToSpans", "spans")).build()))
        .thenAnswer(invocation -> selectedFields.stream());
  }

  private void mockResult(List<Span> spans) {
    when(mockSpanDao.getSpans(any(SpanRequest.class)))
        .thenAnswer(invocation -> Single.just(new TestSpanResultSet(spans)));
  }

  @Value
  private static class TestJoinSource {
    List<String> spanIds;
  }

  private static class TestJoinSourceIdGetter implements MultipleSpanIdGetter<TestJoinSource> {
    @Override
    public Single<List<String>> getSpanIds(TestJoinSource source) {
      return Single.just(source.getSpanIds());
    }
  }

  @Value
  @Accessors(fluent = true)
  private static class TestSpanResultSet implements SpanResultSet {
    List<Span> results;
    long count = 0;
    long total = 0;
  }

  @Value
  @Accessors(fluent = true)
  private static class TestSpan implements Span {
    String id;

    @Override
    public Object attribute(AttributeExpression expression) {
      return null;
    }

    @Override
    public LogEventResultSet logEvents() {
      return null;
    }
  }
}
