import org.gradle.api.JavaVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*

plugins {
    idea
    kotlin("jvm") version "1.2.21"
    id("cn.bestwu.plugin-publish") version "0.0.17"
}

group = "cn.bestwu.gradle"
version = "0.0.1-SNAPSHOT"

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF-8"
}

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    compile(gradleApi())
    compile("org.jetbrains.kotlin:kotlin-stdlib:1.2.21")
    compile("cn.bestwu.gradle:profile-plugin:1.4.7-SNAPSHOT")
    compile("cn.bestwu.gradle:apidoc-plugin:1.2.5-SNAPSHOT")

    testCompile("org.jetbrains.kotlin:kotlin-test-junit:1.2.21")
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