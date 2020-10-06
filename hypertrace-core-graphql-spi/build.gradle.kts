plugins {
  `java-library`
}

dependencies {
  api(platform(project(":hypertrace-core-graphql-platform")))

  api("com.graphql-java:graphql-java")
  api("io.github.graphql-java:graphql-java-annotations")
  // This one's not in the example platform project, so requires a version - else would fail to build
  api("com.google.code.findbugs:jsr305:3.0.2")

}
