package org.hypertrace.core.graphql.span.dao;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.reactivex.rxjava3.core.Single;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.span.request.SpanRequest;
import org.hypertrace.core.graphql.span.schema.SpanResultSet;
import org.hypertrace.core.graphql.utils.grpc.GraphQlGrpcContextBuilder;
import org.hypertrace.gateway.service.GatewayServiceGrpc.GatewayServiceFutureStub;
import org.hypertrace.gateway.service.v1.span.SpansRequest;
import org.hypertrace.gateway.service.v1.span.SpansResponse;

@Singleton
class GatewayServiceSpanDao implements SpanDao {
  private static final int DEFAULT_DEADLINE_SEC = 10;
  private final GatewayServiceFutureStub gatewayServiceStub;
  private final GraphQlGrpcContextBuilder grpcContextBuilder;
  private final GatewayServiceSpanRequestBuilder requestBuilder;
  private final GatewayServiceSpanConverter spanConverter;
  private final SpanLogEventFetcher spanLogEventFetcher;

  @Inject
  GatewayServiceSpanDao(
      GatewayServiceFutureStub gatewayServiceFutureStub,
      GraphQlGrpcContextBuilder grpcContextBuilder,
      GatewayServiceSpanRequestBuilder requestBuilder,
      GatewayServiceSpanConverter spanConverter,
      SpanLogEventFetcher spanLogEventFetcher) {
    this.grpcContextBuilder = grpcContextBuilder;
    this.requestBuilder = requestBuilder;
    this.spanConverter = spanConverter;
    this.spanLogEventFetcher = spanLogEventFetcher;
    this.gatewayServiceStub = gatewayServiceFutureStub;
  }

  @Override
  public Single<SpanResultSet> getSpans(SpanRequest request) {
    return this.requestBuilder
        .buildRequest(request)
        .flatMap(
            serverRequest -> this.makeRequest(request.spanEventsRequest().context(), serverRequest))
        .flatMap(serverResponse -> spanLogEventFetcher.fetchLogEvents(request, serverResponse))
        .flatMap(
            spanLogEventsResponse -> this.spanConverter.convert(request, spanLogEventsResponse));
  }

  private Single<SpansResponse> makeRequest(GraphQlRequestContext context, SpansRequest request) {
    return Single.fromFuture(
        this.grpcContextBuilder
            .build(context)
            .callInContext(
                () ->
                    this.gatewayServiceStub
                        .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                        .getSpans(request)));
  }
}
