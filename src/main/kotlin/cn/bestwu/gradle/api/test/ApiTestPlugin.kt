package cn.bestwu.gradle.api.test

import cn.bestwu.apidoc.ApidocExtension
import cn.bestwu.gradle.apidoc.ApidocPlugin
import cn.bestwu.gradle.profile.ProfileExtension
import cn.bestwu.gradle.profile.ProfilePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.plugins.*
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class ApiTestPlugin : Plugin<Project> {

    @Suppress("DEPRECATION")
    override fun apply(project: Project) {
        project.plugins.apply(JavaPlugin::class.java)
        project.plugins.apply(ApplicationPlugin::class.java)
        project.plugins.apply(WarPlugin::class.java)
        project.plugins.apply(ApidocPlugin::class.java)
        project.plugins.apply(ProfilePlugin::class.java)

        val version = project.findProperty("api.test.version") ?: "1.3.12"
        val starterDocVersion = project.findProperty("api.starter-doc.version") ?: "1.2.10"
        project.configurations.create(Companion.API_TEST_COMPILE_CONFIGURATION_NAME)
        project.dependencies.add(Companion.API_TEST_COMPILE_CONFIGURATION_NAME, "cn.bestwu:api-test:$version")
        project.dependencies.add("compileOnly", "cn.bestwu:starter-apidoc:$starterDocVersion")
        project.dependencies.add("testCompile", "cn.bestwu:starter-apidoc:$starterDocVersion")

        val paths = ((project.findProperty("api.test.paths")
                ?: "_t") as String).split(",").filter { it.isNotBlank() }.map { it.trim() }

        project.extensions.configure(ApidocExtension::class.java) {
            val applicationHost = project.findProperty("generator.application.host") as? String
            if (!applicationHost.isNullOrBlank())
                it.defaultHost = applicationHost!!
            val applicationName = project.findProperty("application.name") as? String
            if (!applicationName.isNullOrBlank())
                it.projectName = applicationName!!
        }
        val configuration = project.configurations.getByName(API_TEST_COMPILE_CONFIGURATION_NAME)
        val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        sourceSet.runtimeClasspath += configuration

        project.extensions.configure(ProfileExtension::class.java) {
            it.closure {
                if (!it.releases.contains(it.active) && !tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).state.executed) {
                    project.extensions.getByType(DistributionContainer::class.java).getAt(DistributionPlugin.MAIN_DISTRIBUTION_NAME).contents {
                        val startScripts = project.tasks.getByName(ApplicationPlugin.TASK_START_SCRIPTS_NAME) as CreateStartScripts
                        val addConfiguration = configuration - project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME)
                        startScripts.classpath += addConfiguration
                        it.from(addConfiguration) {
                            it.into("lib")
                        }
                    }
                    sourceSet.runtimeClasspath = SimpleFileCollection((sourceSet.runtimeClasspath).distinct())
                }
            }
            it.releaseClosure {
                project.tasks.getByName("mddoc").enabled = false
                project.tasks.getByName("htmldoc").enabled = false
                val processResources = project.tasks.getByName("processResources") as ProcessResources
                processResources.exclude(paths)
                sourceSet.runtimeClasspath -= configuration - project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME)
            }
        }

        (project.convention.plugins["application"] as ApplicationPluginConvention).applicationDefaultJvmArgs += "-Dfile.encoding=UTF-8"

        project.extensions.getByType(DistributionContainer::class.java).getAt(DistributionPlugin.MAIN_DISTRIBUTION_NAME).contents {
            it.from((project.tasks.getByName("processResources") as ProcessResources).destinationDir) {
                it.into("resources")
            }
        }

        project.tasks.getByName("compileJava") {
            it.dependsOn("htmldoc", "processResources")
        }
        project.tasks.getByName("jar") {
            it.enabled = true
            it as Jar
            it.dependsOn("generateRebel")
            it.exclude {
                (project.tasks.getByName("processResources") as ProcessResources).destinationDir.listFiles().contains(it.file)
            }
            it.manifest {
                it.attributes(mapOf("Manifest-Version" to version, "Implementation-Title" to project.property("application.name") as String, "Implementation-Version" to version))
            }
        }
        project.tasks.getByName("war") {
            it.enabled = true
            it.mustRunAfter("clean")
        }
        project.tasks.getByName("distZip") {
            it.mustRunAfter("clean")
        }
        project.tasks.getByName("startScripts") {
            it as CreateStartScripts
            it.classpath.add(project.files("\$APP_HOME/resources"))
            it.doLast {
                it as CreateStartScripts
                it.unixScript.writeText(it.unixScript.readText()
                        .replace("\$APP_HOME/lib/resources", "\$APP_HOME/resources")
                )
                it.windowsScript.writeText(it.windowsScript.readText()
                        .replace("%APP_HOME%\\lib\\resources", "%APP_HOME%\\resources")
                )
            }
        }
    }

    companion object {
        private const val API_TEST_COMPILE_CONFIGURATION_NAME = "apiTestCompile"
    }

}