plugins {
    id("java")
    id("maven-publish")
}

group = "cc.irori"
version = "1.2.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.hytale-modding.info/releases/")
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly(libs.multiplehud)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "IroriPoweredMaven"
            url = uri("https://maven.irori.cc/repository/public/")
            credentials {
                username = project.findProperty("irori_maven_username")?.toString() ?: ""
                password = project.findProperty("irori_maven_password")?.toString() ?: ""
            }
        }
    }
}
