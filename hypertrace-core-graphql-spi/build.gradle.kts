plugins {
  `java-library`
}

dependencies {
  api(platform(project(":hypertrace-core-graphql-platform")))

  api("com.graphql-java:graphql-java")
  api("io.github.graphql-java:graphql-java-annotations")
  api("com.google.code.findbugs:jsr305")

}
