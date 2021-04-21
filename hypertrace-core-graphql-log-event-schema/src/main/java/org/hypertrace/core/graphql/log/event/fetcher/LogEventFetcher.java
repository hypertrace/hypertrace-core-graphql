package org.hypertrace.core.graphql.log.event.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.core.graphql.log.event.dao.LogEventDao;
import org.hypertrace.core.graphql.log.event.request.DefaultLogEventRequestBuilder;
import org.hypertrace.core.graphql.log.event.schema.LogEventResultSet;

public class LogEventFetcher extends InjectableDataFetcher<LogEventResultSet> {

  public LogEventFetcher() {
    super(LogEventFetcherImpl.class);
  }

  static final class LogEventFetcherImpl
      implements DataFetcher<CompletableFuture<LogEventResultSet>> {
    private final DefaultLogEventRequestBuilder requestBuilder;
    private final LogEventDao logEventDao;

    @Inject
    LogEventFetcherImpl(DefaultLogEventRequestBuilder requestBuilder, LogEventDao logEventDao) {
      this.requestBuilder = requestBuilder;
      this.logEventDao = logEventDao;
    }

    @Override
    public CompletableFuture<LogEventResultSet> get(DataFetchingEnvironment environment) {
      return this.requestBuilder
          .build(
              environment.getContext(), "LOG_EVENT",
              environment.getArguments(), environment.getSelectionSet())
          .flatMap(this.logEventDao::getLogEvents)
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
