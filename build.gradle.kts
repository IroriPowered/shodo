plugins {
    id("java")
}

group = "cc.irori"
version = "1.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.hytale-modding.info/releases/")
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly(libs.multiplehud)
}
