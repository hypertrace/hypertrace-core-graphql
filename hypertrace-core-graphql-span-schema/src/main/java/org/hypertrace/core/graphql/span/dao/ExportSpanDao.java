package org.hypertrace.core.graphql.span.dao;

import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.core.graphql.span.schema.ExportSpanResult;
import org.hypertrace.core.graphql.span.schema.Span;
import org.hypertrace.core.graphql.span.schema.SpanResultSet;
import org.hypertrace.core.graphql.utils.export.span.ExportSpan;
import org.hypertrace.core.graphql.utils.export.span.ExportSpanAttributesKey;
import org.hypertrace.core.graphql.utils.export.span.ExportSpanResponse;
import org.hypertrace.core.graphql.utils.export.span.ExportSpanResponse.Builder;
import org.hypertrace.core.graphql.utils.export.span.ExportSpanTagsKey;

public class ExportSpanDao {
  private final SpanDao spanDao;

  private static final List<String> excludeTagKeys =
      List.of(ExportSpanTagsKey.SPAN_KIND, ExportSpanTagsKey.STATUS_CODE, ExportSpanTagsKey.ERROR);

  private static final List<String> statusCodeKeys =
      List.of(ExportSpanTagsKey.STATUS_CODE, ExportSpanTagsKey.ERROR);

  @Inject
  ExportSpanDao(SpanDao spanDao) {
    this.spanDao = spanDao;
  }

  public Single<ExportSpanResult> getSpans(SpanRequest request) {
    return this.spanDao
        .getSpans(request)
        .flatMap(spanResultSet -> this.buildResponse(spanResultSet));
  }

  private Single<ExportSpanResult> buildResponse(SpanResultSet result) throws Exception {
    List<ExportSpan> exportSpans =
        result.results().stream().map(span -> convert(span)).collect(Collectors.toList());
    ExportSpanResponse.Builder builder = new Builder(exportSpans);
    return Single.just(new ExportSpanResultImpl(builder.build().toJson()));
  }

  private ExportSpan convert(Span span) {
    ExportSpan.Builder exportSpanBuilder = new ExportSpan.Builder();
    exportSpanBuilder.setResourceServiceName(
        span.attribute(ExportSpanAttributesKey.SERVICE_NAME).toString());

    exportSpanBuilder.setBytesFields(
        span.attribute(ExportSpanAttributesKey.ID).toString(),
        span.attribute(ExportSpanAttributesKey.TRACE_ID).toString(),
        span.attribute(ExportSpanAttributesKey.PARENT_SPAN_ID).toString());

    exportSpanBuilder.setTimeFields(
        Long.parseLong(span.attribute(ExportSpanAttributesKey.START_TIME).toString()),
        Long.parseLong(span.attribute(ExportSpanAttributesKey.END_TIME).toString()));

    exportSpanBuilder.setName(span.attribute(ExportSpanAttributesKey.NAME).toString());

    Map<String, String> tags = (Map<String, String>) span.attribute(ExportSpanAttributesKey.TAGS);
    exportSpanBuilder.setStatusCode(tags, statusCodeKeys);
    exportSpanBuilder.setSpanKind(tags.get(ExportSpanTagsKey.SPAN_KIND));
    exportSpanBuilder.setAttributes(tags, excludeTagKeys);

    return exportSpanBuilder.build();
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ExportSpanResultImpl implements ExportSpanResult {
    String result;
  }
}
