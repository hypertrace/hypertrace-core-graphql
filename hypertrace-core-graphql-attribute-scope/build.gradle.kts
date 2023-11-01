plugins {
  `java-library`
}

dependencies {
  api("com.google.inject:guice")
  api("io.reactivex.rxjava3:rxjava")
  api(projects.hypertraceCoreGraphqlAttributeStore)
  api(projects.hypertraceCoreGraphqlCommonSchema)
  // These are kept in a separate project so they can be referenced by other projects without circular dependencies
  compileOnly(projects.hypertraceCoreGraphqlAttributeScopeConstants)
}
