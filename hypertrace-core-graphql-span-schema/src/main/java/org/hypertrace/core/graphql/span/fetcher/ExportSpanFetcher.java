package org.hypertrace.core.graphql.span.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.core.graphql.span.dao.ExportSpanDao;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.core.graphql.span.request.SpanRequestBuilder;
import org.hypertrace.core.graphql.span.schema.ExportSpanResult;
import org.hypertrace.core.graphql.utils.export.span.ExportSpanAttributesKey;

public class ExportSpanFetcher extends InjectableDataFetcher<ExportSpanResult> {

  private static List<String> spanAttributesKey =
      List.of(
          ExportSpanAttributesKey.ID,
          ExportSpanAttributesKey.SERVICE_NAME,
          ExportSpanAttributesKey.TRACE_ID,
          ExportSpanAttributesKey.PARENT_SPAN_ID,
          ExportSpanAttributesKey.START_TIME,
          ExportSpanAttributesKey.END_TIME,
          ExportSpanAttributesKey.NAME,
          ExportSpanAttributesKey.TAGS);

  public ExportSpanFetcher() {
    super(ExportSpanFetcherImpl.class);
  }

  static final class ExportSpanFetcherImpl
      implements DataFetcher<CompletableFuture<ExportSpanResult>> {
    private final SpanRequestBuilder requestBuilder;
    private final ExportSpanDao exportSpanDao;

    @Inject
    ExportSpanFetcherImpl(SpanRequestBuilder requestBuilder, ExportSpanDao exportSpanDao) {
      this.requestBuilder = requestBuilder;
      this.exportSpanDao = exportSpanDao;
    }

    @Override
    public CompletableFuture<ExportSpanResult> get(DataFetchingEnvironment environment) {
      Single<SpanRequest> spanRequest =
          this.requestBuilder.build(
              environment.getContext(),
              HypertraceCoreAttributeScopeString.SPAN,
              environment.getArguments(),
              spanAttributesKey,
              List.of());

      return spanRequest
          .flatMap(this.exportSpanDao::getSpans)
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
