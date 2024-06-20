package org.hypertrace.core.graphql.utils.gateway;

import static io.reactivex.rxjava3.core.Single.zip;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.LogicalFilterOperator;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.Operator;

class FilterConverter
    implements Converter<Collection<AttributeAssociation<FilterArgument>>, Filter>,
        BiConverter<
            Collection<AttributeAssociation<FilterArgument>>, LogicalFilterOperator, Filter> {

  private final AttributeExpressionConverter attributeExpressionConverter;
  private final OperatorConverter operatorConverter;
  private final LiteralConstantExpressionConverter literalConstantExpressionConverter;

  @Inject
  FilterConverter(
      AttributeExpressionConverter attributeExpressionConverter,
      OperatorConverter operatorConverter,
      LiteralConstantExpressionConverter literalConstantExpressionConverter) {
    this.attributeExpressionConverter = attributeExpressionConverter;
    this.operatorConverter = operatorConverter;
    this.literalConstantExpressionConverter = literalConstantExpressionConverter;
  }

  @Override
  public Single<Filter> convert(Collection<AttributeAssociation<FilterArgument>> filters) {
    return this.convert(filters, LogicalFilterOperator.AND);
  }

  @Override
  public Single<Filter> convert(
      Collection<AttributeAssociation<FilterArgument>> filters, LogicalFilterOperator operator) {
    if (filters.isEmpty()) {
      return Single.just(Filter.getDefaultInstance());
    }

    return Observable.fromIterable(filters)
        .flatMapSingle(this::buildFilter)
        .collect(Collectors.toUnmodifiableList())
        .map(
            filterList ->
                Filter.newBuilder()
                    .setOperator(this.convertLogicalOperator(operator))
                    .addAllChildFilter(filterList)
                    .build());
  }

  private Operator convertLogicalOperator(LogicalFilterOperator logicalFilterOperator) {
    switch (logicalFilterOperator) {
      case OR:
        return Operator.OR;
      case AND:
      default:
        return Operator.AND;
    }
  }

  private Single<Filter> buildFilter(AttributeAssociation<FilterArgument> filter) {
    return zip(
        this.attributeExpressionConverter.convert(
            AttributeAssociation.of(filter.attribute(), filter.value().keyExpression())),
        this.operatorConverter.convert(filter.value().operator()),
        this.literalConstantExpressionConverter.convert(filter.value().value()),
        (key, operator, value) ->
            Filter.newBuilder().setLhs(key).setOperator(operator).setRhs(value).build());
  }
}
