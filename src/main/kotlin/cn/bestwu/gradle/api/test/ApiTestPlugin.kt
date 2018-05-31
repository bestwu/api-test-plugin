package cn.bestwu.gradle.api.test

import cn.bestwu.apidoc.ApidocExtension
import cn.bestwu.gradle.apidoc.ApidocPlugin
import cn.bestwu.gradle.profile.ProfileExtension
import cn.bestwu.gradle.profile.ProfilePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.application.CreateStartScripts
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
        project.plugins.apply(ApidocPlugin::class.java)
        project.plugins.apply(ProfilePlugin::class.java)

        val version = project.findProperty("apitest.version") ?: "1.3.18"
        val starterDocVersion = project.findProperty("apidoc.version") ?: "1.2.66"
        project.configurations.create(API_TEST_COMPILE_CONFIGURATION_NAME)
        project.dependencies.add(API_TEST_COMPILE_CONFIGURATION_NAME, "cn.bestwu:api-test:$version")
        project.dependencies.add("compileOnly", "cn.bestwu:starter-apidoc:$starterDocVersion")
        project.dependencies.add("testCompile", "cn.bestwu:starter-apidoc:$starterDocVersion")

        project.extensions.configure(ApidocExtension::class.java) {
            val applicationHost = project.findProperty("apidoc.defaultHost") as? String
            if (!applicationHost.isNullOrBlank())
                it.defaultHost = applicationHost!!
            val applicationName = project.findProperty("application.name") as? String
            if (!applicationName.isNullOrBlank())
                it.projectName = applicationName!!
        }

        project.tasks.getByName("mddoc").mustRunAfter("clean")

        project.tasks.getByName("compileJava") {
            it.dependsOn("htmldoc")
        }

        val configuration = project.configurations.getByName(API_TEST_COMPILE_CONFIGURATION_NAME)
        val addConfiguration = configuration - project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME)
        val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        sourceSet.runtimeClasspath += addConfiguration
        project.extensions.configure(ProfileExtension::class.java) {
            it.closure {
                if (!it.releases.contains(it.active) && !tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).state.executed) {
                    project.extensions.getByType(DistributionContainer::class.java).getAt(DistributionPlugin.MAIN_DISTRIBUTION_NAME).contents {
                        val startScripts = project.tasks.getByName(ApplicationPlugin.TASK_START_SCRIPTS_NAME) as CreateStartScripts
                        startScripts.classpath += addConfiguration
                        it.from(addConfiguration) {
                            it.into("lib")
                        }
                    }
                }
            }
            it.releaseClosure {
                project.tasks.getByName("mddoc").enabled = false
                project.tasks.getByName("htmldoc").enabled = false
                val processResources = project.tasks.getByName("processResources") as ProcessResources
                processResources.exclude("doc")
                sourceSet.runtimeClasspath -= addConfiguration
            }
        }
    }

    companion object {
        private const val API_TEST_COMPILE_CONFIGURATION_NAME = "apiTestCompile"
    }

}