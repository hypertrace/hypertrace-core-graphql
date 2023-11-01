plugins {
  `java-library`
  jacoco
  alias(commonLibs.plugins.hypertrace.jacoco)
}

dependencies {
  api("com.google.inject:guice")
  api("com.graphql-java:graphql-java")
  api("io.grpc:grpc-api")
  api("io.grpc:grpc-core")
  api("io.grpc:grpc-stub")
  api(projects.hypertraceCoreGraphqlContext)
  api("org.hypertrace.core.grpcutils:grpc-context-utils")

  implementation("org.hypertrace.core.grpcutils:grpc-client-utils")
  implementation("io.grpc:grpc-context")
  implementation("io.reactivex.rxjava3:rxjava")
  implementation("org.slf4j:slf4j-api")
  implementation(projects.hypertraceCoreGraphqlSpi)

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")

  testRuntimeOnly("io.grpc:grpc-netty")
}

tasks.test {
  useJUnitPlatform()
}
