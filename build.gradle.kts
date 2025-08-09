import java.io.File

version = "1.0"

plugins {
    kotlin("jvm") version "1.5.21"
    java
}

val mindustryVersion: String by extra("v146")
val sdkRoot: String? by extra { System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT") }

sourceSets.main.get().java.srcDirs("src")

repositories {
    mavenCentral()
    maven { url = uri("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository") }
    maven { url = uri("https://www.jitpack.io") }
}

dependencies {
    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

task("jarAndroid") {
    dependsOn("jar")

    doLast {
        val sdkRoot: String? = project.extra.get("sdkRoot") as? String
        if (sdkRoot.isNullOrEmpty() || !File(sdkRoot).exists()) {
            throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
        }

        val platformRoot = File("$sdkRoot/platforms/").listFiles()?.sortedDescending()?.find { File(it, "android.jar").exists() }

        if (platformRoot == null) {
            throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")
        }

        //collect dependencies needed for desugaring
        val dependencies = (configurations.compileClasspath.get().files + configurations.runtimeClasspath.get().files + File(platformRoot, "android.jar"))
            .joinToString(" ") { "--classpath ${it.path}" }

        //dex and desugar files - this requires d8 in your PATH
        project.exec {
            workingDir = file("$buildDir/libs")
            commandLine("d8", *dependencies.split(" ").toTypedArray(), "--min-api", "14", "--output", "${project.name}Android.jar", "${project.name}Desktop.jar")
            standardOutput = System.out
            errorOutput = System.err
        }
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${project.name}Desktop.jar")

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    from(rootDir) {
        include("mod.hjson")
    }

    from("assets/") {
        include("**")
    }
}

tasks.register<Jar>("deploy") {
    dependsOn(tasks.named("jarAndroid"))
    dependsOn(tasks.named("jar"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${project.name}.jar")

    from(zipTree("$buildDir/libs/${project.name}Desktop.jar"), zipTree("$buildDir/libs/${project.name}Android.jar"))

    doLast {
        delete("$buildDir/libs/${project.name}Desktop.jar")
        delete("$buildDir/libs/${project.name}Android.jar")
    }
}
