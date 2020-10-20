package org.hypertrace.core.graphql.atttributes.scopes;

import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;

enum HypertraceCoreAttributeScope implements AttributeScope {
  TRACE(HypertraceCoreAttributeScopeString.TRACE),
  SPAN(HypertraceCoreAttributeScopeString.SPAN, HypertraceCoreAttributeScopeString.SPAN_EXTERNAL);

  private final String scope;
  private final String externalScope;

  HypertraceCoreAttributeScope(String scope) {
    this(scope, scope);
  }

  HypertraceCoreAttributeScope(String scope, String externalScope) {
    this.scope = scope;
    this.externalScope = externalScope;
  }

  @Override
  public String getScopeString() {
    return this.scope;
  }

  @Override
  public String getExternalScopeString() {
    return this.externalScope;
  }
}
