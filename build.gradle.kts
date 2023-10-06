import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.jvm").version("1.9.10")
  id("org.jetbrains.intellij").version("1.15.0")
  id("com.diffplug.spotless").version("6.21.0")
}

group = "com.gitlab.lae.intellij.jump"

version = "0.2.3-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  testImplementation("nz.lae.stacksrc:stacksrc-junit5:0.5.0")
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
  testImplementation("org.junit.vintage:junit-vintage-engine:5.10.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

kotlin { jvmToolchain { languageVersion.set(JavaLanguageVersion.of(11)) } }

intellij { version.set("2021.1") }

spotless {
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
}

tasks {
  test {
    useJUnitPlatform()
    systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
    testLogging { exceptionFormat = TestExceptionFormat.FULL }
  }
  patchPluginXml { untilBuild.set("") }
}
