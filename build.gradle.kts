import org.gradle.api.JavaVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaTask

val kotlinVersion = "1.2.41"

plugins {
    idea
    kotlin("jvm") version "1.2.41"
    id("cn.bestwu.plugin-publish") version "0.0.30"
}

group = "cn.bestwu.gradle"
version = "0.0.58"

repositories {
    mavenLocal()
    gradlePluginPortal()
    jcenter()
}

dependencies {
    compile(gradleApi())
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile("gradle.plugin.cn.bestwu.gradle:profile-plugin:1.4.19")
    compile("gradle.plugin.cn.bestwu.gradle:apidoc-plugin:1.2.66")

    testCompile("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

idea {
    module {
        inheritOutputDirs = false
        isDownloadJavadoc = false
        isDownloadSources = true
        outputDir = java.sourceSets["main"].java.outputDir
        testOutputDir = java.sourceSets["test"].java.outputDir
    }
}

tasks {
    "dokkaJavadoc"(DokkaTask::class) {
        noStdlibLink = true
    }
}