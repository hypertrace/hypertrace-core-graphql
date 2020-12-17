plugins {
  `java-platform`
}

dependencies {
  constraints {

    api("org.hypertrace.core.grpcutils:grpc-context-utils:0.3.2")
    api("org.hypertrace.core.grpcutils:grpc-client-utils:0.3.2")
    api("org.hypertrace.gateway.service:gateway-service-api:0.1.40")
    api("org.hypertrace.core.attribute.service:attribute-service-api:0.8.7")

    api("com.google.inject:guice:4.2.3")
    api("com.graphql-java:graphql-java:15.0")
    api("io.github.graphql-java:graphql-java-annotations:8.3")
    api("org.slf4j:slf4j-api:1.7.30")
    api("io.reactivex.rxjava3:rxjava:3.0.7")
    api("com.google.protobuf:protobuf-java-util:3.14.0")
    api("org.projectlombok:lombok:1.18.16")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("com.typesafe:config:1.4.1")
    api("com.google.guava:guava:30.0-jre")
    api("com.graphql-java-kickstart:graphql-java-servlet:10.0.0")
    api("io.grpc:grpc-api:1.33.1")
    api("io.grpc:grpc-core:1.33.1")
    api("io.grpc:grpc-stub:1.33.1")
    api("io.grpc:grpc-context:1.33.1")
    api("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.0")

    runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
    runtime("io.grpc:grpc-netty:1.33.1")
    runtime("io.netty:netty-codec-http2:4.1.53.Final") {
      because("https://snyk.io/vuln/SNYK-JAVA-IONETTY-1020439")
    }
    runtime("io.netty:netty-handler-proxy:4.1.53.Final") {
      because("https://snyk.io/vuln/SNYK-JAVA-IONETTY-1020439s")
    }
  }
}
