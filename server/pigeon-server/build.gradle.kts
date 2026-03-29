plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.pigeonServer"
version = "0.1.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

application {
    mainClass = "pigeonServer.Launcher"
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("io.sentry:sentry:8.37.1")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "pigeonServer.Launcher"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
