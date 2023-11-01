plugins {
  `java-library`
  jacoco
  alias(commonLibs.plugins.hypertrace.jacoco)
}

dependencies {
  api(projects.hypertraceCoreGraphqlSpi)
  api("com.graphql-java-kickstart:graphql-java-servlet")
  api("org.hypertrace.core.grpcutils:grpc-client-utils")

  implementation(projects.hypertraceCoreGraphqlSchemaRegistry)
  implementation(projects.hypertraceCoreGraphqlContext)
  implementation(projects.hypertraceCoreGraphqlDeserialization)
  implementation(projects.hypertraceCoreGraphqlGrpcUtils)
  implementation(projects.hypertraceCoreGraphqlSchemaUtils)
  implementation(projects.hypertraceCoreGraphqlGatewayServiceUtils)
  implementation(projects.hypertraceCoreGraphqlAttributeStore)
  implementation(projects.hypertraceCoreGraphqlCommonSchema)
  implementation(projects.hypertraceCoreGraphqlMetadataSchema)
  implementation(projects.hypertraceCoreGraphqlSpanSchema)
  implementation(projects.hypertraceCoreGraphqlTraceSchema)
  implementation(projects.hypertraceCoreGraphqlAttributeScope)
  implementation(projects.hypertraceCoreGraphqlRxUtils)
  implementation(projects.hypertraceCoreGraphqlLogEventSchema)
  implementation(projects.hypertraceCoreGraphqlRequestTransformation)

  implementation("org.slf4j:slf4j-api")
  implementation("com.google.inject:guice")

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
