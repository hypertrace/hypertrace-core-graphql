plugins {
  `java-library`
}

dependencies {
  api(platform(project(":hypertrace-core-graphql-platform")))
  
  api("com.google.inject:guice")
  api("io.reactivex.rxjava3:rxjava")
  api(project(":hypertrace-core-graphql-attribute-store"))
  api(project(":hypertrace-core-graphql-common-schema"))
}
