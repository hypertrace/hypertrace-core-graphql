package org.hypertrace.core.graphql.log.event.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.attributes.AttributeModelType;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.log.event.request.LogEventRequest;

class BaseDaoTest {

  @Value
  @Accessors(fluent = true)
  static class DefaultLogEventRequest implements LogEventRequest {

    GraphQlRequestContext context;
    Collection<AttributeRequest> attributes;
    TimeRangeArgument timeRange;
    int limit;
    int offset;
    List<AttributeAssociation<OrderArgument>> orderArguments;
    Collection<AttributeAssociation<FilterArgument>> filterArguments;
  }

  @Value
  @Accessors(fluent = true)
  static class DefaultAttributeRequest implements AttributeRequest {

    AttributeModel attribute;

    @Override
    public String alias() {
      return attribute.id();
    }
  }

  @Value
  @Accessors(fluent = true)
  class DefaultAttributeModel implements AttributeModel {

    String id;
    String scope;
    String key;
    String displayName;
    AttributeModelType type;
    String units;
    boolean onlySupportsGrouping;
    boolean onlySupportsAggregation;
    List<AttributeModelMetricAggregationType> supportedMetricAggregationTypes;
    boolean groupable;
  }

  @Value
  @Accessors(fluent = true)
  class DefaultTimeRange implements TimeRangeArgument {

    @JsonProperty(TIME_RANGE_ARGUMENT_START_TIME)
    Instant startTime;

    @JsonProperty(TIME_RANGE_ARGUMENT_END_TIME)
    Instant endTime;
  }
}
