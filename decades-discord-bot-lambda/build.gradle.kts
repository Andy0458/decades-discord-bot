/*
 * This file was generated by the Gradle "init" task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the "Building Java & JVM projects" chapter in the Gradle
 * User Manual available at https://docs.gradle.org/8.0.2/userguide/building_java_projects.html
 */
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("kapt") version "1.9.22"

    // For building a fat JAR
    id("com.github.johnrengelman.shadow") version "8.1.1"

    // KtLint
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

apply(plugin = "com.github.johnrengelman.shadow")
tasks.jar {
    enabled = false
}
tasks.shadowJar {
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
}
tasks.build {
    dependsOn(tasks.shadowJar)
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.0.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.+")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:2.23.0")

    implementation("software.amazon.awssdk:secretsmanager:2.24.9")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.6.0")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
}

// Dagger
dependencies {
    val daggerVersion = "2.50"
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
}

apply(plugin = "org.jlleitschuh.gradle.ktlint")
ktlint {
    ignoreFailures = true
}
tasks.check {
    dependsOn(tasks.ktlintFormat)
}

tasks.test {
    useJUnitPlatform()
    exclude("**/E2ETest.class")
    testLogging.events =
        setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT,
        )
    testLogging.exceptionFormat = TestExceptionFormat.FULL
    testLogging.showExceptions = true
    testLogging.showCauses = true
    testLogging.showStackTraces = true
    // Kotlin DSL workaround from https://github.com/gradle/kotlin-dsl-samples/issues/836#issuecomment-384206237
    addTestListener(
        object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}

            override fun beforeTest(testDescriptor: TestDescriptor) {}

            override fun afterTest(
                testDescriptor: TestDescriptor,
                result: TestResult,
            ) {}

            override fun afterSuite(
                suite: TestDescriptor,
                result: TestResult,
            ) {
                if (suite.parent == null) {
                    val output =
                        "Results: ${result.resultType} (${result.testCount} tests, " +
                            "${result.successfulTestCount} passed, ${result.failedTestCount} failed, " +
                            "${result.skippedTestCount} skipped)"
                    val startItem = "|  "
                    val endItem = "  |"
                    val repeatLength = startItem.length + output.length + endItem.length
                    println("\n" + "-".repeat(repeatLength) + "\n" + startItem + output + endItem + "\n" + "-".repeat(repeatLength))
                }
            }
        },
    )
}
