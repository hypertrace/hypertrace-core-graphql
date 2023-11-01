plugins {
  `java-library`
  jacoco
  alias(commonLibs.plugins.hypertrace.jacoco)
}

dependencies {
  api("com.google.inject:guice")
  api(projects.hypertraceCoreGraphqlSpi)
  api(projects.hypertraceCoreGraphqlContext)

  implementation("org.slf4j:slf4j-api")
  implementation("io.reactivex.rxjava3:rxjava")
  implementation("com.google.guava:guava")

  implementation("org.hypertrace.core.attribute.service:caching-attribute-service-client")
  implementation("org.hypertrace.core.attribute.service:attribute-service-api")
  implementation("org.hypertrace.core.grpcutils:grpc-client-rx-utils")
  implementation(projects.hypertraceCoreGraphqlGrpcUtils)
  implementation(projects.hypertraceCoreGraphqlRxUtils)

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
