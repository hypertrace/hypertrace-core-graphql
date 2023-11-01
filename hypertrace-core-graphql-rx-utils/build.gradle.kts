plugins {
  `java-library`
}

dependencies {
  api("io.reactivex.rxjava3:rxjava")
  api("com.google.inject:guice")
  implementation(projects.hypertraceCoreGraphqlSpi)
  implementation("com.google.guava:guava")
}
