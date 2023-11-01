plugins {
  `java-library`
  jacoco
  alias(commonLibs.plugins.hypertrace.jacoco)
}

dependencies {
  api("com.google.inject:guice")
  api("org.hypertrace.gateway.service:gateway-service-api")
  api(projects.hypertraceCoreGraphqlAttributeStore)
  api("io.reactivex.rxjava3:rxjava")
  api(projects.hypertraceCoreGraphqlCommonSchema)
  implementation(projects.hypertraceCoreGraphqlGrpcUtils)

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
