package org.hypertrace.core.graphql.attributes;

import static org.hypertrace.core.attribute.service.v1.AggregateFunction.AVG;
import static org.hypertrace.core.attribute.service.v1.AggregateFunction.AVGRATE;
import static org.hypertrace.core.attribute.service.v1.AggregateFunction.COUNT;
import static org.hypertrace.core.attribute.service.v1.AggregateFunction.DISTINCT_ARRAY;
import static org.hypertrace.core.attribute.service.v1.AggregateFunction.DISTINCT_COUNT;
import static org.hypertrace.core.attribute.service.v1.AggregateFunction.MAX;
import static org.hypertrace.core.attribute.service.v1.AggregateFunction.MIN;
import static org.hypertrace.core.attribute.service.v1.AggregateFunction.PERCENTILE;
import static org.hypertrace.core.attribute.service.v1.AggregateFunction.SUM;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_BOOL;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_DOUBLE;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_INT64;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_STRING;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_STRING_ARRAY;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_STRING_MAP;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_TIMESTAMP;

import com.google.common.collect.ImmutableBiMap;
import java.util.List;
import java.util.Optional;
import java.util.UnknownFormatConversionException;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.v1.AggregateFunction;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeModelTranslator {
  private static final Logger LOGGER = LoggerFactory.getLogger(AttributeModelTranslator.class);
  private static final ImmutableBiMap<AttributeKind, AttributeModelType> TYPE_MAPPING =
      ImmutableBiMap.<AttributeKind, AttributeModelType>builder()
          .put(TYPE_STRING, AttributeModelType.STRING)
          .put(TYPE_BOOL, AttributeModelType.BOOLEAN)
          .put(TYPE_INT64, AttributeModelType.LONG)
          .put(TYPE_DOUBLE, AttributeModelType.DOUBLE)
          .put(TYPE_TIMESTAMP, AttributeModelType.TIMESTAMP)
          .put(TYPE_STRING_MAP, AttributeModelType.STRING_MAP)
          .put(TYPE_STRING_ARRAY, AttributeModelType.STRING_ARRAY)
          .build();

  private static final ImmutableBiMap<AggregateFunction, AttributeModelMetricAggregationType>
      AGGREGATION_TYPE_MAPPING =
          ImmutableBiMap.<AggregateFunction, AttributeModelMetricAggregationType>builder()
              .put(COUNT, AttributeModelMetricAggregationType.COUNT)
              .put(AVG, AttributeModelMetricAggregationType.AVG)
              .put(SUM, AttributeModelMetricAggregationType.SUM)
              .put(MIN, AttributeModelMetricAggregationType.MIN)
              .put(MAX, AttributeModelMetricAggregationType.MAX)
              .put(AVGRATE, AttributeModelMetricAggregationType.AVGRATE)
              .put(PERCENTILE, AttributeModelMetricAggregationType.PERCENTILE)
              .put(DISTINCT_COUNT, AttributeModelMetricAggregationType.DISTINCT_COUNT)
              .put(DISTINCT_ARRAY, AttributeModelMetricAggregationType.DISTINCT_ARRAY)
              .build();

  public Optional<AttributeModel> translate(AttributeMetadata attributeMetadata) {
    try {
      return Optional.of(
          DefaultAttributeModel.builder()
              .id(attributeMetadata.getId())
              .scope(attributeMetadata.getScopeString())
              .key(attributeMetadata.getKey())
              .displayName(attributeMetadata.getDisplayName())
              .type(this.convertType(attributeMetadata.getValueKind()))
              .units(attributeMetadata.getUnit())
              .onlySupportsGrouping(attributeMetadata.getOnlyAggregationsAllowed())
              .onlySupportsAggregation(attributeMetadata.getType().equals(AttributeType.METRIC))
              .supportedMetricAggregationTypes(
                  this.convertMetricAggregationFunctions(
                      attributeMetadata.getSupportedAggregationsList()))
              .groupable(attributeMetadata.getGroupable())
              .isCustom(attributeMetadata.getCustom())
              .build());
    } catch (Exception e) {
      LOGGER.warn("Dropping attribute {} : {}", attributeMetadata.getId(), e.getMessage());
      return Optional.empty();
    }
  }

  public AttributeMetadata translate(final AttributeModel attributeMetadata) {
    return AttributeMetadata.newBuilder()
        .setScopeString(attributeMetadata.scope())
        .setKey(attributeMetadata.key())
        .setDisplayName(attributeMetadata.displayName())
        .setValueKind(this.convertType(attributeMetadata.type()))
        .setUnit(attributeMetadata.units())
        .setOnlyAggregationsAllowed(attributeMetadata.onlySupportsGrouping())
        .setType(
            attributeMetadata.onlySupportsAggregation()
                ? AttributeType.METRIC
                : AttributeType.ATTRIBUTE)
        .addAllSupportedAggregations(
            this.convertMetricAggregationTypes(attributeMetadata.supportedMetricAggregationTypes()))
        .setGroupable(attributeMetadata.groupable())
        .build();
  }

  public AttributeKind convertType(AttributeModelType type) {
    return Optional.ofNullable(TYPE_MAPPING.inverse().get(type))
        .orElseThrow(
            () ->
                new UnknownFormatConversionException(
                    String.format("Unrecognized attribute type %s", type.name())));
  }

  private List<AttributeModelMetricAggregationType> convertMetricAggregationFunctions(
      List<AggregateFunction> aggregationTypes) {
    return aggregationTypes.stream()
        .map(this::convertMetricAggregationType)
        .collect(Collectors.toUnmodifiableList());
  }

  private List<AggregateFunction> convertMetricAggregationTypes(
      List<AttributeModelMetricAggregationType> aggregationTypes) {
    return aggregationTypes.stream()
        .map(this::convertMetricAggregationType)
        .collect(Collectors.toUnmodifiableList());
  }

  private AttributeModelMetricAggregationType convertMetricAggregationType(
      AggregateFunction aggregateFunction) {
    return Optional.ofNullable(AGGREGATION_TYPE_MAPPING.get(aggregateFunction))
        .orElseThrow(
            () ->
                new UnknownFormatConversionException(
                    String.format("Unrecognized aggregate function %s", aggregateFunction.name())));
  }

  private AggregateFunction convertMetricAggregationType(
      AttributeModelMetricAggregationType aggregationType) {
    return Optional.ofNullable(AGGREGATION_TYPE_MAPPING.inverse().get(aggregationType))
        .orElseThrow(
            () ->
                new UnknownFormatConversionException(
                    String.format("Unrecognized aggregate type %s", aggregationType.name())));
  }

  private AttributeModelType convertType(AttributeKind kind) {
    return Optional.ofNullable(TYPE_MAPPING.get(kind))
        .orElseThrow(
            () ->
                new UnknownFormatConversionException(
                    String.format("Unrecognized attribute kind %s", kind.name())));
  }
}
