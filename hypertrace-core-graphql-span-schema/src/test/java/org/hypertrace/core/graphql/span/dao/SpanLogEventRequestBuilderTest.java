package org.hypertrace.core.graphql.span.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.grpc.CallCredentials;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;
import org.hypertrace.core.graphql.utils.gateway.GatewayUtilsModule;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.Operator;
import org.hypertrace.gateway.service.v1.log.events.LogEventsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpanLogEventRequestBuilderTest extends BaseDaoTest {

  @Mock private FilterRequestBuilder filterRequestBuilder;

  private SpanLogEventRequestBuilder spanLogEventRequestBuilder;

  @BeforeEach
  void beforeEach() {
    Injector injector =
        Guice.createInjector(
            new GatewayUtilsModule(),
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(CallCredentials.class).toInstance(mock(CallCredentials.class));
                bind(GraphQlServiceConfig.class).toInstance(mock(GraphQlServiceConfig.class));
                bind(GrpcChannelRegistry.class).toInstance(mock(GrpcChannelRegistry.class));
              }
            });

    Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter =
        injector.getInstance(
            Key.get(
                new TypeLiteral<
                    Converter<Collection<AttributeAssociation<FilterArgument>>, Filter>>() {}));

    Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter =
        injector.getInstance(
            Key.get(
                new TypeLiteral<Converter<Collection<AttributeRequest>, Set<Expression>>>() {}));

    spanLogEventRequestBuilder =
        new SpanLogEventRequestBuilder(attributeConverter, filterConverter, filterRequestBuilder);
  }

  @Test
  void testBuildRequest() {
    doAnswer(
            invocation -> {
              Set<FilterArgument> filterArguments = invocation.getArgument(2, Set.class);
              FilterArgument filterArgument = filterArguments.iterator().next();
              return Single.just(
                  List.of(
                      AttributeAssociation.of(
                          spanIdAttribute.attribute(),
                          new NormalizedFilter(
                              spanIdAttribute.attribute().key(),
                              filterArgument.operator(),
                              filterArgument.value()))));
            })
        .when(filterRequestBuilder)
        .build(any(), any(), anyCollection());

    long startTime = System.currentTimeMillis();
    long endTime = System.currentTimeMillis() + Duration.ofHours(1).toMillis();

    Collection<AttributeRequest> logAttributeRequests =
        List.of(spanIdAttribute, traceIdAttribute, attributesAttribute);
    ResultSetRequest resultSetRequest =
        new DefaultResultSetRequest(
            null,
            List.of(eventIdAttribute),
            new DefaultTimeRange(Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime)),
            eventIdAttribute,
            0,
            0,
            List.of(),
            Collections.emptyList(),
            Optional.empty());
    SpanRequest spanRequest = new DefaultSpanRequest(resultSetRequest, logAttributeRequests);

    LogEventsRequest logEventsRequest =
        spanLogEventRequestBuilder.buildLogEventsRequest(spanRequest, spansResponse).blockingGet();

    assertEquals(Operator.IN, logEventsRequest.getFilter().getChildFilter(0).getOperator());
    assertEquals(
        spanIdAttribute.attribute().id(),
        logEventsRequest
            .getFilter()
            .getChildFilter(0)
            .getLhs()
            .getColumnIdentifier()
            .getColumnName());
    assertEquals(
        List.of("span1", "span2", "span3"),
        logEventsRequest
            .getFilter()
            .getChildFilter(0)
            .getRhs()
            .getLiteral()
            .getValue()
            .getStringArrayList()
            .stream()
            .collect(Collectors.toList()));
    assertEquals(startTime, logEventsRequest.getStartTimeMillis());
    assertEquals(endTime, logEventsRequest.getEndTimeMillis());
    assertEquals(
        Set.of("attributes", "traceId", "spanId"),
        logEventsRequest.getSelectionList().stream()
            .map(v -> v.getColumnIdentifier().getColumnName())
            .collect(Collectors.toSet()));
  }
}
