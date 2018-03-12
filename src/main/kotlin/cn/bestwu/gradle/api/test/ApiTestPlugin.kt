package cn.bestwu.gradle.api.test

import cn.bestwu.apidoc.ApidocExtension
import cn.bestwu.gradle.apidoc.ApidocPlugin
import cn.bestwu.gradle.profile.ProfileExtension
import cn.bestwu.gradle.profile.ProfilePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.jvm.tasks.Jar

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class ApiTestPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(ApidocPlugin::class.java)
        project.plugins.apply(ProfilePlugin::class.java)

        val version = project.findProperty("api.test.version") ?: "1.3.9"
        val starterDocVersion = project.findProperty("api.starter-doc.version") ?: "0.0.4"
        project.configurations.create(Companion.API_TEST_COMPILE_CONFIGURATION_NAME)
        project.dependencies.add(Companion.API_TEST_COMPILE_CONFIGURATION_NAME, "cn.bestwu:api-test:$version")
        project.dependencies.add("compileOnly", "cn.bestwu:starter-apidoc:$starterDocVersion")
        project.dependencies.add("testCompile", "cn.bestwu:starter-apidoc:$starterDocVersion")

        val paths = ((project.findProperty("api.test.paths")
                ?: "_t") as String).split(",").filter { it.isNotBlank() }.map { it.trim() }

        project.tasks.getByName("clean") {
            it.doFirst {
                paths.forEach {
                    project.file("src/main/resources/$it/md").deleteRecursively()
                    project.file("src/main/resources/$it/html").deleteRecursively()
                }
            }
        }

        project.extensions.configure(ApidocExtension::class.java) {
            val applicationHost = project.findProperty("generator.application.host") as? String
            if (!applicationHost.isNullOrBlank())
                it.defaultHost = applicationHost!!
            val applicationName = project.findProperty("application.name") as? String
            if (!applicationName.isNullOrBlank())
                it.projectName = applicationName!!
        }
        project.extensions.configure(ProfileExtension::class.java) {
            it.closure {
                if (!it.releases.contains(it.active) && !tasks.getByName("startScripts").state.executed) {
                    val configuration = project.configurations.getByName(API_TEST_COMPILE_CONFIGURATION_NAME)
                    project.extensions.getByType(DistributionContainer::class.java).getAt(DistributionPlugin.MAIN_DISTRIBUTION_NAME).contents {
                        val startScripts = project.tasks.getByName(ApplicationPlugin.TASK_START_SCRIPTS_NAME) as CreateStartScripts
                        startScripts.classpath += configuration
                        val libChildSpec = project.copySpec()
                        libChildSpec.into("lib")
                        libChildSpec.from(configuration)
                        it.with(libChildSpec)
                    }
                    val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                    sourceSet.runtimeClasspath = SimpleFileCollection((sourceSet.runtimeClasspath + configuration).distinct())
                }
            }
            it.releaseClosure {
                val jar = project.tasks.getByName("jar") as Jar
                jar.exclude(paths)
            }
        }
        try {
            project.tasks.getByName("processResources") {
                it.dependsOn("htmldoc")
            }
        } catch (e: UnknownTaskException) {
            println(e.message)
        }
    }

    companion object {
        private const val API_TEST_COMPILE_CONFIGURATION_NAME = "apiTestCompile"
    }

}