package org.hypertrace.core.graphql.utils.export.span;

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

@lombok.Value
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExportSpan {

  private final ResourceSpans resourceSpans;

  public static class Builder {

    private static final String SERVICE_NAME_KEY = "service.name";
    private static final String SPAN_KIND_PREFIX = "SPAN_KIND";
    private static final String SPANK_KIND_JOINER = "_";

    private final ResourceSpans.Builder resourceSpansBuilder;
    private final Resource.Builder resourceBuilder;
    private final InstrumentationLibrarySpans.Builder instrumentationLibrarySpansBuilder;
    private final Span.Builder spanBuilder;

    public Builder() {
      this.resourceSpansBuilder = ResourceSpans.newBuilder();
      this.resourceBuilder = Resource.newBuilder();
      this.instrumentationLibrarySpansBuilder = InstrumentationLibrarySpans.newBuilder();
      this.spanBuilder = Span.newBuilder();
    }

    public Builder setResourceServiceName(String serviceName) {
      KeyValue keyValue =
          KeyValue.newBuilder()
              .setKey(SERVICE_NAME_KEY)
              .setValue(AnyValue.newBuilder().setStringValue(serviceName).build())
              .build();
      this.resourceBuilder.addAttributes(keyValue);
      return this;
    }

    public Builder setBytesFields(String spanId, String traceId, String parentSpanId) {
      byte[] spanIdBytes = BaseEncoding.base64().decode(spanId);
      spanBuilder.setSpanId(ByteString.copyFrom(spanIdBytes));

      byte[] traceIdBytes = BaseEncoding.base64().decode(traceId);
      spanBuilder.setTraceId(ByteString.copyFrom(traceIdBytes));

      byte[] parentSpanIdBytes = BaseEncoding.base64().decode("");
      if (parentSpanId != null) {
        parentSpanIdBytes = BaseEncoding.base64().decode(parentSpanId);
      }
      spanBuilder.setParentSpanId(ByteString.copyFrom(parentSpanIdBytes));
      return this;
    }

    public Builder setTimeFields(long startTime, long endTime) {
      spanBuilder.setStartTimeUnixNano(
          TimeUnit.NANOSECONDS.convert(startTime, TimeUnit.MILLISECONDS));
      spanBuilder.setEndTimeUnixNano(TimeUnit.NANOSECONDS.convert(endTime, TimeUnit.MILLISECONDS));
      return this;
    }

    public Builder setName(String name) {
      spanBuilder.setName(name);
      return this;
    }

    public Builder setAttributes(Map<String, String> tags, List<String> excludeKeys) {
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
      return this;
    }

    public Builder setStatusCode(Map<String, String> tags, List<String> statusCodeKeys) {
      int statusCode =
          statusCodeKeys.stream()
              .filter(e -> tags.containsKey(e))
              .map(e -> Integer.parseInt(tags.get(e)))
              .findFirst()
              .orElse(0);
      spanBuilder.setStatus(Status.newBuilder().setCode(StatusCode.forNumber(statusCode)).build());
      return this;
    }

    public Builder setSpanKind(String spanKind) {
      if (spanKind != null) {
        spanBuilder.setKind(
            SpanKind.valueOf(
                String.join(SPANK_KIND_JOINER, SPAN_KIND_PREFIX, spanKind.toUpperCase())));
      } else {
        spanBuilder.setKind(SpanKind.SPAN_KIND_UNSPECIFIED);
      }
      return this;
    }

    public ExportSpan build() {
      this.resourceSpansBuilder.setResource(resourceBuilder.build());
      instrumentationLibrarySpansBuilder.addSpans(spanBuilder.build());
      this.resourceSpansBuilder.addInstrumentationLibrarySpans(
          instrumentationLibrarySpansBuilder.build());
      return new ExportSpan(this.resourceSpansBuilder.build());
    }
  }
}
