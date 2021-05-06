import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
  `java-library`
  id("com.google.protobuf")
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.15.7"
  }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.builtins {
        java
      }
    }
  }
}

sourceSets {
  main {
    java {
      srcDirs("src/main/java", "build/generated/source/proto/main/java")
    }
  }
}

dependencies {
  api("com.google.inject:guice")

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")

  implementation("com.google.protobuf:protobuf-java-util")
  implementation("com.google.guava:guava")
  implementation("org.apache.commons:commons-text")
  implementation("com.fasterxml.jackson.core:jackson-databind")
}
