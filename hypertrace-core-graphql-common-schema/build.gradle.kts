plugins {
  `java-library`
  jacoco
  alias(commonLibs.plugins.hypertrace.jacoco)
}

dependencies {
  api("com.google.inject:guice")
  api("com.graphql-java:graphql-java")
  api(projects.hypertraceCoreGraphqlAttributeStore)
  api(projects.hypertraceCoreGraphqlContext)
  api("io.reactivex.rxjava3:rxjava")
  api("io.github.graphql-java:graphql-java-annotations")

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")

  compileOnly(projects.hypertraceCoreGraphqlAttributeScopeConstants)

  implementation(projects.hypertraceCoreGraphqlDeserialization)
  implementation(projects.hypertraceCoreGraphqlSchemaUtils)

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
