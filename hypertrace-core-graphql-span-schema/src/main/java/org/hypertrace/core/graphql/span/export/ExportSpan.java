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

    private final ResourceSpans.Builder resourceSpansBuilder;
    private final Resource.Builder resourceBuilder;
    private final InstrumentationLibrarySpans.Builder instrumentationLibrarySpansBuilder;
    private final Span.Builder spanBuilder;
    private final org.hypertrace.core.graphql.span.schema.Span span;

    public Builder(org.hypertrace.core.graphql.span.schema.Span span) {
      this.resourceSpansBuilder = ResourceSpans.newBuilder();
      this.resourceBuilder = Resource.newBuilder();
      this.instrumentationLibrarySpansBuilder = InstrumentationLibrarySpans.newBuilder();
      this.spanBuilder = Span.newBuilder();
      this.span = span;
    }

    private void setResourceServiceName(String serviceName) {
      KeyValue keyValue =
          KeyValue.newBuilder()
              .setKey(SERVICE_NAME_KEY)
              .setValue(AnyValue.newBuilder().setStringValue(serviceName).build())
              .build();
      this.resourceBuilder.addAttributes(keyValue);
    }

    private void setBytesFields(String spanId, String traceId, String parentSpanId) {
      byte[] spanIdBytes = BaseEncoding.base64().decode(spanId);
      spanBuilder.setSpanId(ByteString.copyFrom(spanIdBytes));

      byte[] traceIdBytes = BaseEncoding.base64().decode(traceId);
      spanBuilder.setTraceId(ByteString.copyFrom(traceIdBytes));

      byte[] parentSpanIdBytes = BaseEncoding.base64().decode("");
      if (parentSpanId != null) {
        parentSpanIdBytes = BaseEncoding.base64().decode(parentSpanId);
      }
      spanBuilder.setParentSpanId(ByteString.copyFrom(parentSpanIdBytes));
    }

    public void setTimeFields(long startTime, long endTime) {
      spanBuilder.setStartTimeUnixNano(
          TimeUnit.NANOSECONDS.convert(startTime, TimeUnit.MILLISECONDS));
      spanBuilder.setEndTimeUnixNano(TimeUnit.NANOSECONDS.convert(endTime, TimeUnit.MILLISECONDS));
    }

    public void setName(String name) {
      spanBuilder.setName(name);
    }

    public void setAttributes(Map<String, String> tags, List<String> excludeKeys) {
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

    private void setStatusCode(Map<String, String> tags, List<String> statusCodeKeys) {
      int statusCode =
          statusCodeKeys.stream()
              .filter(e -> tags.containsKey(e))
              .map(e -> Integer.parseInt(tags.get(e)))
              .findFirst()
              .orElse(0);
      spanBuilder.setStatus(Status.newBuilder().setCode(StatusCode.forNumber(statusCode)).build());
    }

    private void setSpanKind(String spanKind) {
      if (spanKind != null) {
        spanBuilder.setKind(
            SpanKind.valueOf(String.join("_", "SPAN_KIND", spanKind.toUpperCase())));
      } else {
        spanBuilder.setKind(SpanKind.SPAN_KIND_UNSPECIFIED);
      }
    }

    public ExportSpan build() {

      setResourceServiceName(span.attribute(SpanAttributes.SERVICE_NAME).toString());

      setBytesFields(
          span.attribute(SpanAttributes.ID).toString(),
          span.attribute(SpanAttributes.TRACE_ID).toString(),
          span.attribute(SpanAttributes.PARENT_SPAN_ID).toString());

      setTimeFields(
          Long.parseLong(span.attribute(SpanAttributes.START_TIME).toString()),
          Long.parseLong(span.attribute(SpanAttributes.END_TIME).toString()));

      setName(span.attribute(SpanAttributes.NAME).toString());

      Map<String, String> tags = (Map<String, String>) span.attribute(SpanAttributes.TAGS);
      setStatusCode(tags, SpanTagsKey.getStatusCodeKeys());
      setSpanKind(tags.get(SPAN_KIND));
      setAttributes(tags, SpanTagsKey.getExcludeKeys());

      resourceSpansBuilder.setResource(resourceBuilder.build());
      instrumentationLibrarySpansBuilder.addSpans(spanBuilder.build());
      resourceSpansBuilder.addInstrumentationLibrarySpans(
          instrumentationLibrarySpansBuilder.build());

      return new ExportSpan(resourceSpansBuilder.build());
    }
  }
}
