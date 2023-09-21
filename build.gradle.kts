import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.jvm").version("1.9.10")
  id("org.jetbrains.intellij").version("1.15.0")
  id("com.diffplug.spotless").version("6.21.0")
}

group = "com.gitlab.lae.intellij.jump"

version = "0.2.3-SNAPSHOT"

repositories { mavenCentral() }

kotlin { jvmToolchain { languageVersion.set(JavaLanguageVersion.of(11)) } }

intellij { version.set("2021.1") }

spotless {
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
}

tasks {
  test { testLogging { exceptionFormat = TestExceptionFormat.FULL } }
  patchPluginXml { untilBuild.set("") }
}
