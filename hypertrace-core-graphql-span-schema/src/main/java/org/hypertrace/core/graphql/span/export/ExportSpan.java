package org.hypertrace.core.graphql.span.export;

import static org.hypertrace.core.graphql.span.export.ExportSpanConstants.SpanTagsKey.SERVICE_NAME_KEY;
import static org.hypertrace.core.graphql.span.export.ExportSpanConstants.SpanTagsKey.SPAN_KIND;

import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.proto.trace.v1.Status.StatusCode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.span.export.ExportSpanConstants.SpanAttributes;
import org.hypertrace.core.graphql.span.export.ExportSpanConstants.SpanTagsKey;

@lombok.Value
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExportSpan {

  private final ResourceSpans resourceSpans;

  public static class Builder {

    private final org.hypertrace.core.graphql.span.schema.Span span;

    public Builder(org.hypertrace.core.graphql.span.schema.Span span) {
      this.span = span;
    }

    private void setResourceServiceName(Resource.Builder resourceBuilder) {
      if (span.attribute(SpanAttributes.SERVICE_NAME) != null) {
        String serviceName = span.attribute(SpanAttributes.SERVICE_NAME).toString();
        KeyValue keyValue =
            KeyValue.newBuilder()
                .setKey(SERVICE_NAME_KEY)
                .setValue(AnyValue.newBuilder().setStringValue(serviceName).build())
                .build();
        resourceBuilder.addAttributes(keyValue);
      }
    }

    private void setBytesFields(Span.Builder spanBuilder) {
      byte[] spanIdBytes = BaseEncoding.base64().decode(span.id());
      spanBuilder.setSpanId(ByteString.copyFrom(spanIdBytes));

      String traceId = span.attribute(SpanAttributes.TRACE_ID).toString();
      byte[] traceIdBytes = BaseEncoding.base64().decode(traceId);
      spanBuilder.setTraceId(ByteString.copyFrom(traceIdBytes));

      byte[] parentSpanIdBytes = BaseEncoding.base64().decode("");
      if (span.attribute(SpanAttributes.PARENT_SPAN_ID) != null) {
        parentSpanIdBytes =
            BaseEncoding.base64().decode(span.attribute(SpanAttributes.PARENT_SPAN_ID).toString());
      }
      spanBuilder.setParentSpanId(ByteString.copyFrom(parentSpanIdBytes));
    }

    private void setTimeFields(Span.Builder spanBuilder) {
      long startTime = Long.parseLong(span.attribute(SpanAttributes.START_TIME).toString());
      spanBuilder.setStartTimeUnixNano(
          TimeUnit.NANOSECONDS.convert(startTime, TimeUnit.MILLISECONDS));

      long endTime = Long.parseLong(span.attribute(SpanAttributes.END_TIME).toString());
      spanBuilder.setEndTimeUnixNano(TimeUnit.NANOSECONDS.convert(endTime, TimeUnit.MILLISECONDS));
    }

    private void setName(Span.Builder spanBuilder) {
      if (span.attribute(SpanAttributes.NAME) != null) {
        spanBuilder.setName(span.attribute(SpanAttributes.NAME).toString());
      }
    }

    private static void setAttributes(
        Span.Builder spanBuilder, Map<String, String> tags, List<String> excludeKeys) {
      List<KeyValue> attributes =
          tags.entrySet().stream()
              .filter(e -> !excludeKeys.contains(e.getKey()))
              .map(
                  e ->
                      KeyValue.newBuilder()
                          .setKey(e.getKey())
                          .setValue(AnyValue.newBuilder().setStringValue(e.getValue()).build())
                          .build())
              .collect(Collectors.toList());
      spanBuilder.addAllAttributes(attributes);
    }

    private static void setStatusCode(
        Span.Builder spanBuilder, Map<String, String> tags, List<String> statusCodeKeys) {
      int statusCode =
          statusCodeKeys.stream()
              .filter(e -> tags.containsKey(e))
              .map(e -> Integer.parseInt(tags.get(e)))
              .findFirst()
              .orElse(0);
      spanBuilder.setStatus(Status.newBuilder().setCode(StatusCode.forNumber(statusCode)).build());
    }

    private static void setSpanKind(Span.Builder spanBuilder, String spanKind) {
      if (spanKind != null) {
        spanBuilder.setKind(
            SpanKind.valueOf(String.join("_", "SPAN_KIND", spanKind.toUpperCase())));
      } else {
        spanBuilder.setKind(SpanKind.SPAN_KIND_UNSPECIFIED);
      }
    }

    public ExportSpan build() {

      ResourceSpans.Builder resourceSpansBuilder = ResourceSpans.newBuilder();
      Resource.Builder resourceBuilder = Resource.newBuilder();
      InstrumentationLibrarySpans.Builder instrumentationLibrarySpansBuilder =
          InstrumentationLibrarySpans.newBuilder();
      Span.Builder spanBuilder = Span.newBuilder();

      setResourceServiceName(resourceBuilder);
      setBytesFields(spanBuilder);
      setTimeFields(spanBuilder);
      setName(spanBuilder);

      Map<String, String> tags =
          span.attribute(SpanAttributes.TAGS) != null
              ? (Map<String, String>) span.attribute(SpanAttributes.TAGS)
              : Map.of();
      setStatusCode(spanBuilder, tags, SpanTagsKey.getStatusCodeKeys());
      setSpanKind(spanBuilder, tags.get(SPAN_KIND));
      setAttributes(spanBuilder, tags, SpanTagsKey.getExcludeKeys());

      resourceSpansBuilder.setResource(resourceBuilder.build());
      instrumentationLibrarySpansBuilder.addSpans(spanBuilder.build());
      resourceSpansBuilder.addInstrumentationLibrarySpans(
          instrumentationLibrarySpansBuilder.build());

      return new ExportSpan(resourceSpansBuilder.build());
    }
  }
}
