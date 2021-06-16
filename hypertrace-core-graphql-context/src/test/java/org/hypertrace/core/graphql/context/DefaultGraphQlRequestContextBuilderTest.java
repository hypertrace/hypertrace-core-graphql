package org.hypertrace.core.graphql.context;

import static java.util.Collections.emptyMap;
import static org.hypertrace.core.graphql.context.DefaultGraphQlRequestContextBuilder.AUTHORIZATION_HEADER_KEY;
import static org.hypertrace.core.graphql.context.DefaultGraphQlRequestContextBuilder.TENANT_ID_HEADER_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import graphql.schema.DataFetcher;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultGraphQlRequestContextBuilderTest {

  @Mock Injector mockInjector;
  @Mock HttpServletRequest mockRequest;
  @Mock HttpServletResponse mockResponse;
  @Mock GraphQlServiceConfig mockServiceConfig;

  GraphQlRequestContextBuilder contextBuilder;
  GraphQlRequestContext requestContext;

  @BeforeEach
  void beforeEach() {
    this.contextBuilder =
        new DefaultGraphQlRequestContextBuilder(this.mockInjector, this.mockServiceConfig);
    this.requestContext = this.contextBuilder.build(this.mockRequest, this.mockResponse);
  }

  @Test
  void returnsAuthorizationHeaderIfPresent() {
    when(this.mockRequest.getHeader(eq(AUTHORIZATION_HEADER_KEY))).thenReturn("Bearer ABC");
    when(this.mockRequest.getHeader(eq(AUTHORIZATION_HEADER_KEY.toLowerCase())))
        .thenReturn("Bearer abc");
    assertEquals(Optional.of("Bearer ABC"), this.requestContext.getAuthorizationHeader());

    when(this.mockRequest.getHeader(eq(AUTHORIZATION_HEADER_KEY))).thenReturn(null);
    assertEquals(Optional.of("Bearer abc"), this.requestContext.getAuthorizationHeader());
  }

  @Test
  void returnsEmptyOptionalIfNoAuthorizationHeaderPresent() {
    when(this.mockRequest.getHeader(any())).thenReturn(null);
    assertEquals(Optional.empty(), this.requestContext.getAuthorizationHeader());
  }

  @Test
  void delegatesDataLoaderRegistry() {
    assertTrue(this.requestContext.getDataLoaderRegistry().isPresent());
  }

  @Test
  void canConstructDataFetcher() {
    this.requestContext.constructDataFetcher(DataFetcher.class);
    verify(this.mockInjector).getInstance(DataFetcher.class);
  }

  @Test
  void returnsTenantIdIfTenantIdHeaderPresent() {
    when(this.mockRequest.getHeader(TENANT_ID_HEADER_KEY)).thenReturn("test tenant id");
    assertEquals(Optional.of("test tenant id"), this.requestContext.getTenantId());
  }

  @Test
  void returnsDefaultTenantIdOnlyIfNoHeaderPresent() {
    when(this.mockRequest.getHeader(TENANT_ID_HEADER_KEY)).thenReturn("test tenant id");
    when(this.mockServiceConfig.getDefaultTenantId()).thenReturn(Optional.of("default tenant id"));
    assertEquals(Optional.of("test tenant id"), this.requestContext.getTenantId());
    reset(this.mockRequest);
    assertEquals(Optional.of("default tenant id"), this.requestContext.getTenantId());
  }

  @Test
  void returnsCachingKeyForNoAuth() {
    assertNotNull(this.requestContext.getCachingKey());
  }

  @Test
  void returnsCachingKeysEqualForSameTenant() {
    when(this.mockRequest.getHeader(TENANT_ID_HEADER_KEY)).thenReturn("first tenant id");
    var firstKey = this.contextBuilder.build(this.mockRequest, this.mockResponse).getCachingKey();
    var secondKey = this.contextBuilder.build(this.mockRequest, this.mockResponse).getCachingKey();
    assertEquals(firstKey, secondKey);
    assertNotSame(firstKey, secondKey);

    when(this.mockRequest.getHeader(TENANT_ID_HEADER_KEY)).thenReturn("second tenant id");
    var thirdKey = this.contextBuilder.build(this.mockRequest, this.mockResponse).getCachingKey();
    assertNotEquals(firstKey, thirdKey);
  }

  @Test
  void returnsEmptyMapIfNoTracingHeadersPresent() {
    when(this.mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(List.of("foo")));
    assertEquals(emptyMap(), this.requestContext.getTracingContextHeaders());
  }

  @Test
  void returnsLowerCasedTracingHeadersIfAnyMatches() {
    when(this.mockRequest.getHeaderNames())
        .thenReturn(
            Collections.enumeration(
                List.of(
                    "traceSTATE", "traceparent", "other", "X-B3-traceid", "x-b3-parent-trace-id")));
    when(this.mockRequest.getHeader(any(String.class)))
        .thenAnswer(invocation -> invocation.getArgument(0) + " value");
    assertEquals(
        Map.of(
            "tracestate",
            "traceSTATE value",
            "traceparent",
            "traceparent value",
            "x-b3-traceid",
            "X-B3-traceid value",
            "x-b3-parent-trace-id",
            "x-b3-parent-trace-id value"),
        this.requestContext.getTracingContextHeaders());
  }

  @Test
  void returnsRolesIfPresentInJwt() {
    String rolesClaimName = "https://example.com/roles";
    List<String> expectedRoles = ImmutableList.of("user", "admin");
    doReturn(rolesClaimName).when(this.mockServiceConfig).getRolesClaimName();
    doReturn("Bearer " + getJwtWithRoles()).when(this.mockRequest).getHeader("Authorization");
    List<String> actualRoles = this.requestContext.getRoles();
    assertEquals(expectedRoles, actualRoles);
  }

  @Test
  void returnsEmptyRolesIfRolesClaimNameIsInvalid() {
    String rolesClaimName = "invalidRolesClaim";
    doReturn(rolesClaimName).when(this.mockServiceConfig).getRolesClaimName();
    doReturn("Bearer " + getJwtWithRoles()).when(this.mockRequest).getHeader("Authorization");
    List<String> actualRoles = this.requestContext.getRoles();
    assertEquals(Collections.emptyList(), actualRoles);
  }

  @Test
  void returnsEmptyRolesIfRolesClaimNameIsNull() {
    doReturn(null).when(this.mockServiceConfig).getRolesClaimName();
    doReturn("Bearer " + getJwtWithRoles()).when(this.mockRequest).getHeader("Authorization");
    List<String> actualRoles = this.requestContext.getRoles();
    assertEquals(Collections.emptyList(), actualRoles);
  }

  @Test
  void returnsEmptyRolesIfClaimNotPresentInJwt() {
    String rolesClaimName = "https://example.com/roles";
    doReturn(rolesClaimName).when(this.mockServiceConfig).getRolesClaimName();
    doReturn("Bearer " + getJwtWithOutRoles()).when(this.mockRequest).getHeader("Authorization");
    List<String> actualRoles = this.requestContext.getRoles();
    assertEquals(Collections.emptyList(), actualRoles);
  }

  @Test
  void returnsEmptyRolesIfAuthHeaderNotPresent() {
    doReturn(null).when(this.mockRequest).getHeader("Authorization");
    List<String> actualRoles = this.requestContext.getRoles();
    assertEquals(Collections.emptyList(), actualRoles);
  }

  @Test
  void returnsEmptyRolesIfJwtIsInvalid() {
    String rolesClaimName = "https://example.com/roles";
    doReturn(rolesClaimName).when(this.mockServiceConfig).getRolesClaimName();
    doReturn("Bearer ABC").when(this.mockRequest).getHeader("Authorization");
    List<String> actualRoles = this.requestContext.getRoles();
    assertEquals(Collections.emptyList(), actualRoles);
  }

  private String getJwtWithRoles() {
    return "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE2MjEzNjM1OTcsImV4cCI6" +
        "MTY1Mjg5OTU5NywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5u" +
        "eSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJuYW1lIjoiSm9obm55IFJvY2tldCIsImVtYWlsIjoianJvY2tldEBleGFtcGxlLmNvbSIsInBpY3R1" +
        "cmUiOiJ3d3cuZXhhbXBsZS5jb20iLCJodHRwczovL2V4YW1wbGUuY29tL3JvbGVzIjpbInVzZXIiLCJhZG1pbiJdfQ.PKWns1aii5HEOje-8" +
        "vGwvlYcWYMi4LWgw9CUQlc0npM";
  }

  private String getJwtWithOutRoles() {
    return "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE2MjEzNjM1OTcsImV4cCI6" +
        "MTY1Mjg5OTU5NywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5u" +
        "eSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJuYW1lIjoiSm9obm55IFJvY2tldCIsImVtYWlsIjoianJvY2tldEBleGFtcGxlLmNvbSIsInBpY3R1" +
        "cmUiOiJ3d3cuZXhhbXBsZS5jb20ifQ.Ui1Z2RhiVe3tq6uJPgcyjsfDBdeOeINs_gXEHC6cdpU";
  }
}
