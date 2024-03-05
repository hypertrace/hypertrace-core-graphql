package org.hypertrace.core.graphql.log.event.dao;

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
import org.hypertrace.core.graphql.log.event.request.LogEventRequest;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.OrderByExpression;
import org.hypertrace.gateway.service.v1.log.events.LogEventsRequest;

@RequiredArgsConstructor(onConstructor_ = @Inject)
class GatewayServiceLogEventsRequestBuilder {
  private final BiConverter<
          Collection<AttributeAssociation<FilterArgument>>, LogicalFilterOperator, Filter>
      filterConverter;
  private final Converter<List<AttributeAssociation<OrderArgument>>, List<OrderByExpression>>
      orderConverter;
  private final Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter;

  Single<LogEventsRequest> buildRequest(LogEventRequest gqlRequest) {
    return zip(
        this.attributeConverter.convert(gqlRequest.attributes()),
        this.orderConverter.convert(gqlRequest.orderArguments()),
        this.filterConverter.convert(
            gqlRequest.filterArguments(), gqlRequest.logicalFilterOperator()),
        (selections, orderBys, filters) ->
            LogEventsRequest.newBuilder()
                .setStartTimeMillis(gqlRequest.timeRange().startTime().toEpochMilli())
                .setEndTimeMillis(gqlRequest.timeRange().endTime().toEpochMilli())
                .addAllSelection(selections)
                .addAllOrderBy(orderBys)
                .setLimit(gqlRequest.limit())
                .setOffset(gqlRequest.offset())
                .setFilter(filters)
                .build());
  }
}
