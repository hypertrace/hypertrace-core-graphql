package org.hypertrace.core.graphql.rx;

import com.google.inject.AbstractModule;
import io.reactivex.rxjava3.core.Scheduler;
import javax.inject.Singleton;

public class RxUtilModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Scheduler.class)
        .annotatedWith(NetworkScheduler.class)
        .toProvider(NetworkSchedulerProvider.class)
        .in(Singleton.class);
  }
}
