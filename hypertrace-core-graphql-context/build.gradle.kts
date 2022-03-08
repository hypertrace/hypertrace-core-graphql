plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
}

dependencies {
  api("com.google.inject:guice")
  api("com.graphql-java:graphql-java")
  api("com.graphql-java-kickstart:graphql-java-servlet")

  implementation(project(":hypertrace-core-graphql-spi"))
  implementation("com.google.guava:guava")
  implementation("org.hypertrace.core.grpcutils:grpc-context-utils")

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
