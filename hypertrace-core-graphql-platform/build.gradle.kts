plugins {
  `java-platform`
}

dependencies {
  constraints {

    api("org.hypertrace.core.grpcutils:grpc-context-utils:0.7.2")
    api("org.hypertrace.core.grpcutils:grpc-client-utils:0.7.2")
    api("org.hypertrace.core.grpcutils:grpc-client-rx-utils:0.7.2")
    api("org.hypertrace.gateway.service:gateway-service-api:0.2.0")
    api("org.hypertrace.core.attribute.service:caching-attribute-service-client:0.13.6")

    api("com.google.inject:guice:4.2.3")
    api("com.graphql-java:graphql-java:15.0")
    api("io.github.graphql-java:graphql-java-annotations:8.3")
    api("org.slf4j:slf4j-api:1.7.30")
    api("io.reactivex.rxjava3:rxjava:3.0.9")
    api("com.google.protobuf:protobuf-java-util:3.19.2")
    api("com.google.protobuf:protobuf-java:3.19.2") {
      because("https://snyk.io/vuln/SNYK-JAVA-COMGOOGLEPROTOBUF-2331703")
    }

    api("org.projectlombok:lombok:1.18.18")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("com.typesafe:config:1.4.1")
    api("com.google.guava:guava:31.1-jre")
    api("com.graphql-java-kickstart:graphql-java-servlet:10.1.0")
    api("io.grpc:grpc-api:1.45.1")
    api("io.grpc:grpc-core:1.45.1")
    api("io.grpc:grpc-stub:1.45.1")
    api("io.grpc:grpc-context:1.45.1")
    api("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.6")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.6")
    api("org.apache.commons:commons-text:1.9")
    api("io.opentelemetry:opentelemetry-proto:1.1.0-alpha")
    api("com.google.code.gson:gson:2.8.9") {
      because("https://snyk.io/vuln/SNYK-JAVA-COMGOOGLECODEGSON-1730327")
    }

    runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
    runtime("io.grpc:grpc-netty:1.46.0")
    runtime("io.netty:netty-codec-http2:4.1.71.Final")
    runtime("io.netty:netty-handler-proxy:4.1.71.Final")
  }
}
