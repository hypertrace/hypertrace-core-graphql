package org.hypertrace.core.graphql.attributes;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hypertrace.core.graphql.context.ContextualCachingKey;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

@Singleton
class CachingAttributeStore implements AttributeStore {

  private final AttributeClient attributeClient;
  private final IdLookup idLookup;

  @Inject
  CachingAttributeStore(AttributeClient attributeClient, IdLookup idLookup) {
    this.attributeClient = attributeClient;
    this.idLookup = idLookup;
  }

  private final LoadingCache<ContextualCachingKey, Single<Table<String, String, AttributeModel>>>
      cache =
          CacheBuilder.newBuilder()
              .maximumSize(1000)
              .expireAfterWrite(15, TimeUnit.MINUTES)
              .build(CacheLoader.from(this::loadTable));

  @Override
  public Single<List<AttributeModel>> getAll(GraphQlRequestContext context) {
    return this.getOrInvalidate(context).map(table -> List.copyOf(table.values()));
  }

  @Override
  public Single<AttributeModel> get(GraphQlRequestContext context, String scope, String key) {
    return this.getOrInvalidate(context)
        .map(table -> Optional.ofNullable(table.get(scope, key)))
        .flatMapMaybe(Maybe::fromOptional)
        .switchIfEmpty(Single.error(this.buildErrorForMissingAttribute(scope, key)));
  }

  @Override
  public Single<Map<String, AttributeModel>> get(
      GraphQlRequestContext context, String scope, Collection<String> keys) {
    return this.getOrInvalidate(context)
        .flatMap(table -> this.getValuesOrError(scope, table.row(scope), keys));
  }

  @Override
  public Single<AttributeModel> getIdAttribute(GraphQlRequestContext context, String scope) {
    return this.getIdKey(scope).flatMap(key -> this.get(context, scope, key));
  }

  @Override
  public Single<AttributeModel> getForeignIdAttribute(
      GraphQlRequestContext context, String scope, String foreignScope) {
    return this.getForeignIdKey(scope, foreignScope).flatMap(key -> this.get(context, scope, key));
  }

  private Single<Table<String, String, AttributeModel>> loadTable(ContextualCachingKey cachingKey) {
    return this.attributeClient
        .queryAll(cachingKey.getContext())
        .toList()
        .map(this::buildTable)
        .cache();
  }

  private Table<String, String, AttributeModel> buildTable(List<AttributeModel> attributes) {
    return attributes.stream()
        .collect(
            ImmutableTable.toImmutableTable(
                AttributeModel::scope, AttributeModel::key, Function.identity()));
  }

  private Single<Table<String, String, AttributeModel>> getOrInvalidate(
      GraphQlRequestContext context) {
    return this.cache
        .getUnchecked(context.getCachingKey())
        .doOnError(x -> this.cache.invalidate(context.getCachingKey()));
  }

  private Single<String> getForeignIdKey(String scope, String foreignScope) {
    return Maybe.fromOptional(this.idLookup.foreignIdKey(scope, foreignScope))
        .switchIfEmpty(
            Single.error(this.buildErrorForMissingForeignScopeMapping(scope, foreignScope)));
  }

  private Single<String> getIdKey(String scope) {
    return Maybe.fromOptional(this.idLookup.idKey(scope))
        .switchIfEmpty(Single.error(this.buildErrorForMissingIdMapping(scope)));
  }

  private Single<Map<String, AttributeModel>> getValuesOrError(
      String scope,
      Map<String, AttributeModel> definedAttributes,
      Collection<String> requestedAttributeKeys) {
    return Observable.fromIterable(requestedAttributeKeys)
        .flatMap(
            key ->
                definedAttributes.containsKey(key)
                    ? Observable.just(definedAttributes.get(key))
                    : Observable.error(this.buildErrorForMissingAttribute(scope, key)))
        .collect(Collectors.toUnmodifiableMap(AttributeModel::key, Function.identity()));
  }

  private NoSuchElementException buildErrorForMissingAttribute(String scope, String key) {
    return new NoSuchElementException(
        String.format("No attribute available for scope '%s' and key '%s'", scope, key));
  }

  private NoSuchElementException buildErrorForMissingForeignScopeMapping(
      String scope, String foreignScope) {
    return new NoSuchElementException(
        String.format(
            "No id attribute registered for scope '%s' and foreign scope '%s'",
            scope, foreignScope));
  }

  private NoSuchElementException buildErrorForMissingIdMapping(String scope) {
    return new NoSuchElementException(
        String.format("No id attribute registered for scope '%s'", scope));
  }
}
