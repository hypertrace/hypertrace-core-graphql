plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
}

dependencies {
  api(platform(project(":hypertrace-core-graphql-platform")))
  testImplementation(platform(project(":hypertrace-core-graphql-test-platform")))
  
  api("com.google.inject:guice")
  api("org.hypertrace.gateway.service:gateway-service-api")
  api(project(":hypertrace-core-graphql-attribute-store"))
  api("io.reactivex.rxjava3:rxjava")
  api(project(":hypertrace-core-graphql-common-schema"))

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
