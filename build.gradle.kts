plugins {
  alias(commonLibs.plugins.hypertrace.ciutils)
  alias(commonLibs.plugins.hypertrace.codestyle) apply false
  alias(commonLibs.plugins.owasp.dependencycheck)
}

subprojects {
  group = "org.hypertrace.core.graphql"
  pluginManager.withPlugin("java") {
    apply(plugin = commonLibs.plugins.hypertrace.codestyle.get().pluginId)
    configure<JavaPluginExtension> {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
    }
  }

  pluginManager.withPlugin("java-library") {
    dependencies {
      "api"(platform(project(":hypertrace-core-graphql-platform")))
      "annotationProcessor"(platform(project(":hypertrace-core-graphql-platform")))
      "testAnnotationProcessor"(platform(project(":hypertrace-core-graphql-platform")))
      "testImplementation"(platform(project(":hypertrace-core-graphql-test-platform")))
      "compileOnly"(platform(project(":hypertrace-core-graphql-platform")))
    }
  }
}

dependencyCheck {
  format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL.toString()
  suppressionFile = "owasp-suppressions.xml"
  scanConfigurations.add("runtimeClasspath")
  failBuildOnCVSS = 7.0F
}
