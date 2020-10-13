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
  api(project(":hypertrace-core-graphql-spi"))
  api(project(":hypertrace-core-graphql-context"))

  implementation("org.slf4j:slf4j-api")
  implementation("io.reactivex.rxjava3:rxjava")
  implementation("com.google.guava:guava:29.0-jre")

  implementation("org.hypertrace.core.attribute.service:attribute-service-api")
  implementation(project(":hypertrace-core-graphql-grpc-utils"))

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
