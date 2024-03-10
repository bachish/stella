
plugins {
    java
    kotlin("jvm") version "1.9.21"
    id("com.strumenta.antlr-kotlin") version "1.0.0-RC2"
    //for building an executable files
    application
}

group = "org.pl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    // https://mvnrepository.com/artifact/org.antlr/antlr4
    implementation("org.antlr:antlr4:4.13.1")
    // https://github.com/Strumenta/antlr-kotlin
    implementation("com.strumenta:antlr-kotlin-runtime:1.0.0-RC2")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()

}
kotlin {
    jvmToolchain(11)
}



application {
    mainClass = "org.pl.MainKt"
}

