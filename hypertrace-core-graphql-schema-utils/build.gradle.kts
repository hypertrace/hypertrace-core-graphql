plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
}

dependencies {
  api(platform(project(":hypertrace-core-graphql-platform")))
  annotationProcessor(platform(project(":hypertrace-core-graphql-platform")))
  testImplementation(platform(project(":hypertrace-core-graphql-test-platform")))

  api("com.google.inject:guice")
  api("com.graphql-java:graphql-java")

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
