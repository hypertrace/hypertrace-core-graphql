package org.hypertrace.core.graphql.utils.gateway;

import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Value;

class UnwrappedValueConverter implements Converter<Value, Object> {

  private static final long NANOS_IN_SECOND = 1_000_000_000L;

  @Override
  public Single<Object> convert(Value from) {
    Value value = Optional.ofNullable(from).orElse(Value.getDefaultInstance());
    switch (value.getValueType()) {
      case STRING:
        return Single.just(value.getString());
      case BOOL:
        return Single.just(value.getBoolean());
      case LONG:
        return Single.just(value.getLong());
      case DOUBLE:
        return Single.just(value.getDouble());
      case TIMESTAMP:
        if (!StringUtils.isEmpty(value.getTimestampUnit())) {
          if ("ns".equals(value.getTimestampUnit())) {
            return Single.just(
                Instant.ofEpochSecond(
                    value.getTimestamp() / NANOS_IN_SECOND,
                    value.getTimestamp() % NANOS_IN_SECOND));
          } else if ("ms".equals(value.getTimestampUnit())) {
            return Single.just(Instant.ofEpochMilli(value.getTimestamp()));
          }
        }
        // if unit is not present just fallback to millis
        try {
          return Single.just(Instant.ofEpochMilli(value.getTimestamp()));
        } catch (Throwable t) {
          return Single.error(t);
        }
      case STRING_MAP:
        return Single.just(value.getStringMapMap());
      case STRING_ARRAY:
        return Single.just(value.getStringArrayList());
      case LONG_ARRAY:
      case DOUBLE_ARRAY:
      case BOOLEAN_ARRAY:
      case UNSET:
      case UNRECOGNIZED:
      default:
        return Single.error(
            new UnsupportedOperationException(
                String.format(
                    "Cannot convert value for unknown value type '%s'",
                    value.getValueType().name())));
    }
  }
}
