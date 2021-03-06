package org.hypertrace.core.graphql.attributes;

import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

public interface AttributeStore {
  Single<List<AttributeModel>> getAll(GraphQlRequestContext context);

  Single<AttributeModel> get(GraphQlRequestContext context, String scope, String key);

  Single<Map<String, AttributeModel>> get(
      GraphQlRequestContext context, String scope, Collection<String> keys);

  Single<AttributeModel> getIdAttribute(GraphQlRequestContext context, String scope);

  Single<AttributeModel> getForeignIdAttribute(
      GraphQlRequestContext context, String scope, String foreignScope);
}
