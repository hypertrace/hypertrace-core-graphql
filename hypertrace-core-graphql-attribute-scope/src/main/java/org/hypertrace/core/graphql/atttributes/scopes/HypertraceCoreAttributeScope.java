package org.hypertrace.core.graphql.atttributes.scopes;

import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;

enum HypertraceCoreAttributeScope implements AttributeScope {
  TRACE(HypertraceCoreAttributeScopeString.TRACE),
  SPAN(HypertraceCoreAttributeScopeString.SPAN),
  LOG_EVENT(HypertraceCoreAttributeScopeString.LOG_EVENT);

  private final String scope;

  HypertraceCoreAttributeScope(String scope) {
    this.scope = scope;
  }

  @Override
  public String getScopeString() {
    return this.scope;
  }
}
