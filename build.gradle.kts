plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.letmedevelop"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://plugins.jetbrains.com/maven") // Explicit Marketplace repository
}

intellij {
    version.set("2024.1")
    type.set("IU") // Use IntelliJ IDEA Ultimate Edition

    // Remove com.intellij.json dependency since it is part of the base IntelliJ distribution
    plugins.set(listOf())  // Empty list or no plugins specified
    // Alternatively, you can add only plugins you need explicitly
    // plugins.set(listOf("com.intellij.java", "com.intellij.kotlin"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
