plugins {
    id("java-library")
    id("software.amazon.smithy").version("0.6.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    val smithyVersion: String by project

    implementation("software.amazon.smithy:smithy-openapi:$smithyVersion")
    implementation("software.amazon.smithy:smithy-aws-apigateway-openapi:$smithyVersion")
    implementation("software.amazon.smithy:smithy-aws-traits:$smithyVersion")
}
