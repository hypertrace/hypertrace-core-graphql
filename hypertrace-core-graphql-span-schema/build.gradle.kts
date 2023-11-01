plugins {
  `java-library`
}

dependencies {
  api("com.google.inject:guice")
  api("com.graphql-java:graphql-java")
  api(projects.hypertraceCoreGraphqlSpi)
  api("io.github.graphql-java:graphql-java-annotations")

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")

  compileOnly(projects.hypertraceCoreGraphqlAttributeScopeConstants)

  implementation("org.slf4j:slf4j-api")
  implementation("io.reactivex.rxjava3:rxjava")
  implementation("org.hypertrace.gateway.service:gateway-service-api")
  implementation("com.google.protobuf:protobuf-java-util")
  implementation("io.opentelemetry:opentelemetry-proto")
  implementation("org.apache.commons:commons-text")

  implementation(projects.hypertraceCoreGraphqlContext)
  implementation(projects.hypertraceCoreGraphqlGrpcUtils)
  implementation(projects.hypertraceCoreGraphqlCommonSchema)
  implementation(projects.hypertraceCoreGraphqlAttributeStore)
  implementation(projects.hypertraceCoreGraphqlLogEventSchema)
  implementation(projects.hypertraceCoreGraphqlDeserialization)
  implementation(projects.hypertraceCoreGraphqlSchemaUtils)
  implementation(projects.hypertraceCoreGraphqlAttributeScopeConstants)
  implementation(projects.hypertraceCoreGraphqlRequestTransformation)

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation(projects.hypertraceCoreGraphqlGatewayServiceUtils)
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")

  testAnnotationProcessor("org.projectlombok:lombok")
  testCompileOnly("org.projectlombok:lombok")
}

tasks.test {
  useJUnitPlatform()
}
