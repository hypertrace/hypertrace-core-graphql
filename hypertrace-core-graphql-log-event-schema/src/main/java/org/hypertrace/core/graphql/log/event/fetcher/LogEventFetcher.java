package org.hypertrace.core.graphql.log.event.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.log.event.dao.LogEventDao;
import org.hypertrace.core.graphql.log.event.schema.LogEventResultSet;

public class LogEventFetcher extends InjectableDataFetcher<LogEventResultSet> {

  public LogEventFetcher() {
    super(LogEventFetcherImpl.class);
  }

  static final class LogEventFetcherImpl implements
      DataFetcher<CompletableFuture<LogEventResultSet>> {
    private final ResultSetRequestBuilder requestBuilder;
    private final LogEventDao logEventDao;

    @Inject
    LogEventFetcherImpl(ResultSetRequestBuilder requestBuilder, LogEventDao logEventDao) {
      this.requestBuilder = requestBuilder;
      this.logEventDao = logEventDao;
    }

    @Override
    public CompletableFuture<LogEventResultSet> get(DataFetchingEnvironment environment) {
      return this.requestBuilder
          .build(
              environment.getContext(),
              HypertraceCoreAttributeScopeString.SPAN,
              environment.getArguments(),
              environment.getSelectionSet())
          .flatMap(this.logEventDao::getLogEvents)
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}

