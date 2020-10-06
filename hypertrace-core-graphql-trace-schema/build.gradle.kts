plugins {
  `java-library`
}

dependencies {
  api(platform(project(":hypertrace-core-graphql-platform")))

  api("com.google.inject:guice")
  api("com.graphql-java:graphql-java")
  api(project(":hypertrace-core-graphql-spi"))
  api("io.github.graphql-java:graphql-java-annotations")

  // Compile only time things don't require a platform since they're isolated to this project
  annotationProcessor("org.projectlombok:lombok:1.18.12")
  compileOnly("org.projectlombok:lombok:1.18.12")

  implementation("org.slf4j:slf4j-api")
  implementation("io.reactivex.rxjava3:rxjava")
  implementation("org.hypertrace.gateway.service:gateway-service-api")
  implementation("com.google.protobuf:protobuf-java-util")

  implementation(project(":hypertrace-core-graphql-context"))
  implementation(project(":hypertrace-core-graphql-grpc-utils"))
  implementation(project(":hypertrace-core-graphql-common-schema"))
  implementation(project(":hypertrace-core-graphql-attribute-store"))
  implementation(project(":hypertrace-core-graphql-deserialization"))

}
