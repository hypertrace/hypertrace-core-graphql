package org.hypertrace.core.graphql.trace.dao;

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
import org.hypertrace.core.graphql.trace.request.TraceRequest;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.OrderByExpression;
import org.hypertrace.gateway.service.v1.trace.TracesRequest;

@RequiredArgsConstructor(onConstructor_ = @Inject)
class GatewayServiceTraceRequestBuilder {
  private final BiConverter<
          Collection<AttributeAssociation<FilterArgument>>, LogicalFilterOperator, Filter>
      filterConverter;
  private final Converter<List<AttributeAssociation<OrderArgument>>, List<OrderByExpression>>
      orderConverter;
  private final Converter<Collection<AttributeRequest>, Set<Expression>> selectionConverter;

  Single<TracesRequest> buildRequest(TraceRequest request) {

    return zip(
        this.selectionConverter.convert(request.resultSetRequest().attributes()),
        this.orderConverter.convert(request.resultSetRequest().orderArguments()),
        this.filterConverter.convert(
            request.resultSetRequest().filterArguments(),
            request.resultSetRequest().logicalFilterOperator()),
        (selections, orderBys, filters) ->
            TracesRequest.newBuilder()
                .setScope(request.traceType().getScopeString())
                .setStartTimeMillis(
                    request.resultSetRequest().timeRange().startTime().toEpochMilli())
                .setEndTimeMillis(request.resultSetRequest().timeRange().endTime().toEpochMilli())
                .addAllSelection(selections)
                .addAllOrderBy(orderBys)
                .setLimit(request.resultSetRequest().limit())
                .setOffset(request.resultSetRequest().offset())
                .setFilter(filters)
                .setSpaceId(
                    request.resultSetRequest().spaceId().orElse("")) // String proto default value
                .setFetchTotal(request.fetchTotal())
                .build());
  }
}
