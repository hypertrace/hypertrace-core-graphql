plugins {
  java
  application
  alias(commonLibs.plugins.hypertrace.docker.application)
  alias(commonLibs.plugins.hypertrace.docker.publish)
}

dependencies {
  implementation(platform(project(":hypertrace-core-graphql-platform")))

  implementation("org.hypertrace.core.serviceframework:platform-http-service-framework")
  implementation("org.slf4j:slf4j-api")

  implementation("com.graphql-java-kickstart:graphql-java-servlet")
  implementation(projects.hypertraceCoreGraphqlImpl)
  implementation(projects.hypertraceCoreGraphqlSpi)

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
  runtimeOnly("io.grpc:grpc-netty")
}

application {
  mainClass.set("org.hypertrace.core.serviceframework.PlatformServiceLauncher")
}
