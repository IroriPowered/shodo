plugins {
    id("java")
    alias(libs.plugins.gradle.shadow)
}

group = "cc.irori"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.hytale-modding.info/releases/")
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))

    implementation(libs.guava)
    implementation(libs.lang3)

    compileOnly(libs.multiplehud)
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
