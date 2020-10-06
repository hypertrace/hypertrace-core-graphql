plugins {
  `java-platform`
}

dependencies {
  constraints {
    api("com.google.inject:guice:4.2.3")
    api("com.graphql-java:graphql-java:14.0")
    api("io.github.graphql-java:graphql-java-annotations:8.0")
    api("org.slf4j:slf4j-api:1.7.3")
    api("io.reactivex.rxjava3:rxjava:3.0.2")
    api("org.hypertrace.gateway.service:gateway-service-api:0.1.1")
    api("com.google.protobuf:protobuf-java-util:3.11.4")
  }
}