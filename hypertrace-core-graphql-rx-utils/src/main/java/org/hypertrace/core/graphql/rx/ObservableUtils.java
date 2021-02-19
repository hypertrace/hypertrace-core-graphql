package org.hypertrace.core.graphql.rx;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import org.slf4j.Logger;

public class ObservableUtils {

  // Not instantiable
  private ObservableUtils() {}

  /**
   * Logs any encountered error with the provided logger at the error level. Effectively, this skips
   * the causing element, but resumes the observable without affecting other elements.
   *
   * <p>This is intended to be used with the {@link Observable#onErrorResumeNext(Function)
   * onErrorResumeNext} operator
   *
   * @param logger Logger to be used to log the message
   * @param <T> unused type parameter as the provided function provides no values
   * @return a function that can be provided to the {@link Observable#onErrorResumeNext(Function)
   *     onErrorResumeNext} operator
   */
  public static <T>
      Function<? super Throwable, ? extends ObservableSource<? extends T>> logErrorAndSkip(
          Logger logger) {
    return error -> {
      logger.error("Encountered error, dropping value", error);
      return Observable.empty();
    };
  }
}
