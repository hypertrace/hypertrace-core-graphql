package org.hypertrace.core.graphql.utils.export.span;

public interface ExportSpanAttributesKey {
  String ID = "id";
  String SERVICE_NAME = "serviceName";
  String TRACE_ID = "traceId";
  String PARENT_SPAN_ID = "parentSpanId";
  String START_TIME = "startTime";
  String END_TIME = "endTime";
  String NAME = "displaySpanName";
  String TAGS = "spanTags";
}
