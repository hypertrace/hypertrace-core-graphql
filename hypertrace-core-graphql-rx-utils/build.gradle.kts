plugins {
  `java-library`
}

dependencies {
  api(platform(project(":hypertrace-core-graphql-platform")))
  
  api("io.reactivex.rxjava3:rxjava")
  api("com.google.inject:guice")
  implementation(project(":hypertrace-core-graphql-spi"))
  implementation("com.google.guava:guava")
}
