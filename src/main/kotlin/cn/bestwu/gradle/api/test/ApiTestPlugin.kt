package cn.bestwu.gradle.api.test

import cn.bestwu.apidoc.ApidocExtension
import cn.bestwu.gradle.apidoc.ApidocPlugin
import cn.bestwu.gradle.profile.ProfileExtension
import cn.bestwu.gradle.profile.ProfilePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
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

        project.afterEvaluate {
            val version = it.findProperty("api.test.version") ?: "1.3.8"
            val starterDocVersion = it.findProperty("api.starter-doc.version") ?: "0.0.2"
            project.dependencies.add("compile", "cn.bestwu:api-test:$version")
            project.dependencies.add("compileOnly", "cn.bestwu:starter-apidoc:$starterDocVersion")
            project.dependencies.add("testCompile", "cn.bestwu:starter-apidoc:$starterDocVersion")
        }

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
            it.closure = arrayOf()
            it.releaseClosure {
                val jar = project.tasks.getByName("jar") as Jar
                jar.exclude(paths)
                project.configurations.getByName("compile").dependencies.removeIf {
                    it.group == "cn.bestwu" && it.name == "api-test"
                }
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

}