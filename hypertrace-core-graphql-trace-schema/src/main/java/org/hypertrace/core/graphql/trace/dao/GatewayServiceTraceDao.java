package org.hypertrace.core.graphql.trace.dao;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.grpc.CallCredentials;
import io.micrometer.core.instrument.Timer;
import io.reactivex.rxjava3.core.Single;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;
import org.hypertrace.core.graphql.trace.request.TraceRequest;
import org.hypertrace.core.graphql.trace.schema.TraceResultSet;
import org.hypertrace.core.graphql.utils.grpc.GraphQlGrpcContextBuilder;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.core.serviceframework.metrics.PlatformMetricsRegistry;
import org.hypertrace.gateway.service.GatewayServiceGrpc;
import org.hypertrace.gateway.service.GatewayServiceGrpc.GatewayServiceFutureStub;
import org.hypertrace.gateway.service.v1.trace.TracesRequest;
import org.hypertrace.gateway.service.v1.trace.TracesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
class GatewayServiceTraceDao implements TraceDao {
  private static final int DEFAULT_DEADLINE_SEC = 10;
  private final GatewayServiceFutureStub gatewayServiceStub;
  private final GraphQlGrpcContextBuilder grpcContextBuilder;
  private final GatewayServiceTraceRequestBuilder requestBuilder;
  private final GatewayServiceTraceConverter traceConverter;
  private static final String TRACE_FETCH_LATENCY = "graphql.trace.fetch.latency";
  private final ConcurrentMap<String, Timer> tenantToTraceFetchTime = new ConcurrentHashMap<>();
  private static final Logger LOG = LoggerFactory.getLogger(GatewayServiceTraceDao.class);

  @Inject
  GatewayServiceTraceDao(
      GraphQlServiceConfig serviceConfig,
      CallCredentials credentials,
      GraphQlGrpcContextBuilder grpcContextBuilder,
      GrpcChannelRegistry channelRegistry,
      GatewayServiceTraceRequestBuilder requestBuilder,
      GatewayServiceTraceConverter traceConverter) {
    this.grpcContextBuilder = grpcContextBuilder;
    this.requestBuilder = requestBuilder;
    this.traceConverter = traceConverter;

    this.gatewayServiceStub =
        GatewayServiceGrpc.newFutureStub(
                channelRegistry.forAddress(
                    serviceConfig.getGatewayServiceHost(), serviceConfig.getGatewayServicePort()))
            .withCallCredentials(credentials);
  }

  @Override
  public Single<TraceResultSet> getTraces(TraceRequest request) {
    try {
      return tenantToTraceFetchTime.computeIfAbsent(
          request.context().getTenantId().orElse("NOT_AVAILABLE"),
          tenantId ->
            PlatformMetricsRegistry.registerTimer(TRACE_FETCH_LATENCY, Map.of("tenantid", tenantId)))
          .recordCallable(getTracesCallable(request));
    } catch (Exception e) {
      LOG.error("Failed to get traces for tenant {}", request.context().getTenantId());
    }
    return null;
  }

  private Callable<Single<TraceResultSet>> getTracesCallable(TraceRequest request) {
    return () -> this.requestBuilder
        .buildRequest(request)
        .flatMap(serverRequest -> this.makeRequest(request.context(), serverRequest))
        .flatMap(
            serverResponse ->
                this.traceConverter.convert(request.resultSetRequest(), serverResponse));
  }

  private Single<TracesResponse> makeRequest(GraphQlRequestContext context, TracesRequest request) {
    return Single.fromFuture(
        this.grpcContextBuilder
            .build(context)
            .callInContext(
                () ->
                    this.gatewayServiceStub
                        .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                        .getTraces(request)));
  }
}
