package org.hypertrace.core.graphql.span.dao;

import static io.reactivex.rxjava3.core.Single.zip;

import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.LogicalFilterOperator;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.OrderByExpression;
import org.hypertrace.gateway.service.v1.span.SpansRequest;

@RequiredArgsConstructor(onConstructor_ = @Inject)
class GatewayServiceSpanRequestBuilder {

  private final BiConverter<
          Collection<AttributeAssociation<FilterArgument>>, LogicalFilterOperator, Filter>
      filterConverter;
  private final Converter<List<AttributeAssociation<OrderArgument>>, List<OrderByExpression>>
      orderConverter;
  private final Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter;

  Single<SpansRequest> buildRequest(SpanRequest gqlRequest) {
    return zip(
        this.attributeConverter.convert(gqlRequest.spanEventsRequest().attributes()),
        this.orderConverter.convert(gqlRequest.spanEventsRequest().orderArguments()),
        this.filterConverter.convert(
            gqlRequest.spanEventsRequest().filterArguments(),
            gqlRequest.spanEventsRequest().logicalFilterOperator()),
        (selections, orderBys, filters) ->
            SpansRequest.newBuilder()
                .setStartTimeMillis(
                    gqlRequest.spanEventsRequest().timeRange().startTime().toEpochMilli())
                .setEndTimeMillis(
                    gqlRequest.spanEventsRequest().timeRange().endTime().toEpochMilli())
                .addAllSelection(selections)
                .addAllOrderBy(orderBys)
                .setLimit(gqlRequest.spanEventsRequest().limit())
                .setOffset(gqlRequest.spanEventsRequest().offset())
                .setFilter(filters)
                .setSpaceId(
                    gqlRequest
                        .spanEventsRequest()
                        .spaceId()
                        .orElse("")) // String proto default value
                .setFetchTotal(gqlRequest.fetchTotal())
                .build());
  }
}
