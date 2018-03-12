import org.gradle.api.JavaVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*

val kotlinVersion = "1.2.30"

plugins {
    idea
    kotlin("jvm") version "1.2.30"
    id("cn.bestwu.plugin-publish") version "0.0.18"
}

group = "gradle.plugin.cn.bestwu.gradle"
version = "0.0.13"

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF-8"
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    jcenter()
}

dependencies {
    compile(gradleApi())
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile("gradle.plugin.cn.bestwu.gradle:profile-plugin:1.4.14")
    compile("gradle.plugin.cn.bestwu.gradle:apidoc-plugin:1.2.9")

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