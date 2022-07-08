@file:Suppress("RemoveRedundantBackticks")

package io.github.liplum.mindustry

import io.github.liplum.dsl.*
import io.github.liplum.mindustry.task.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.configurationcache.extensions.capitalized
import java.io.File

typealias Mgpp = MindustryPlugin

class MindustryPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.func {
        try {
            plugins.apply<JavaPlugin>()
        } catch (e: Exception) {
            logger.warn("Your project doesn't support java plugin, so mgpp was disabled.", e)
            return@func
        }
        plugins.apply<MindustryAppPlugin>()
        plugins.apply<MindustryAssetPlugin>()
        plugins.apply<MindustryJavaPlugin>()
        GroovyBridge.attach(target)
    }

    companion object {
        /**
         * A task group for main tasks, named `mindustry`
         */
        const val MindustryTaskGroup = "mindustry"
        /**
         * A task group for tasks related to [MindustryAssetsExtension], named `mindustry assets`
         */
        const val MindustryAssetTaskGroup = "mindustry assets"
        /**
         * The name of [MindustryExtension]
         */
        const val MainExtensionName = "mindustry"
        /**
         * The name of [MindustryAssetsExtension]
         */
        const val AssetExtensionName = "mindustryAssets"
        /**
         * The environment variable, as a folder, for Mindustry client to store data
         */
        const val MindustryDataDirEnv = "MINDUSTRY_DATA_DIR"
        /**
         * The default minGameVersion in `mod.(h)json`.
         *
         * **Note:** You shouldn't pretend this version and work based on it.
         */
        const val DefaultMinGameVersion = "135"
        /**
         * [The default Mindustry version](https://github.com/Anuken/Mindustry/releases/tag/v135)
         *
         * **Note:** You shouldn't pretend this version and work based on it.
         */
        const val DefaultMindustryVersion = "v135"
        /**
         * [The default bleeding edge version](https://github.com/Anuken/MindustryBuilds/releases/tag/22767)
         *
         * **Note:** You shouldn't pretend this version and work based on it.
         */
        const val DefaultMindustryBEVersion = "22767"
        /**
         * [The default Arc version](https://github.com/Anuken/Arc/releases/tag/v135.2)
         *
         * **Note:** You shouldn't pretend this version and work based on it.
         */
        const val DefaultArcVersion = "v135"
        /**
         * [Mindustry official repo](https://api.github.com/repos/Anuken/Mindustry/releases/latest)
         */
        const val OfficialReleaseURL = "https://api.github.com/repos/Anuken/MindustryBuilds/releases/latest"
        /**
         * [Mindustry bleeding edge repo](https://api.github.com/repos/Anuken/Mindustry/releases/latest)
         */
        const val BEReleaseURL = "https://api.github.com/repos/Anuken/MindustryBuilds/releases/latest"
        /**
         * [A cat](https://github.com/Anuken)
         */
        const val Anuken = "anuken"
        /**
         * [Mindustry game](https://github.com/Anuken/Mindustry)
         */
        const val Mindustry = "mindustry"
        /**
         * [Mindustry bleeding egde](https://github.com/Anuken/MindustryBuilds)
         */
        const val MindustryBuilds = "MindustryBuilds"
        /**
         * [The name convention of client release](https://github.com/Anuken/Mindustry/releases)
         */
        const val ClientReleaseName = "Mindustry.jar"
        /**
         * [The name convention of server release](https://github.com/Anuken/Mindustry/releases)
         */
        const val ServerReleaseName = "server-release.jar"
        /**
         * (The Mindustry repo on Jitpack)[https://github.com/anuken/mindustry]
         */
        const val MindustryJitpackRepo = "com.github.anuken.mindustry"
        /**
         * (The mirror repo of Mindustry on Jitpack)[https://github.com/anuken/mindustryjitpack]
         */
        const val MindustryJitpackMirrorRepo = "com.github.anuken.mindustryjitpack"
        /**
         * (The GitHub API to fetch the latest commit of mirror)[https://github.com/Anuken/MindustryJitpack/commits/main]
         */
        const val MindustryJitpackLatestCommit = "https://api.github.com/repos/Anuken/MindustryJitpack/commits/main"
        /**
         * (The GitHub API to fetch the latest commit of arc)[https://github.com/Anuken/Arc/commits/master]
         */
        const val ArcLatestCommit = "https://api.github.com/repos/Anuken/Arc/commits/master"
        /**
         * (The Arc repo on Jitpack)[https://github.com/anuken/arc]
         */
        const val ArcJitpackRepo = "com.github.anuken.arc"
        /**
         * The main class of desktop launcher.
         */
        const val MindustryDesktopMainClass = "mindustry.desktop.DesktopLauncher"
        /**
         * The main class of server launcher.
         */
        const val MindustrySeverMainClass = "mindustry.server.ServerLauncher"
        /**
         * An empty folder for null-check
         */
        @JvmStatic
        val DefaultEmptyFile = File("")
    }
}
/**
 * It transports the Jar task output to running task.
 */
class MindustryJavaPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.func {
        val ex = extensions.getOrCreate<MindustryExtension>(
            Mgpp.MainExtensionName
        )
        val dexJar = tasks.register<DexJar>("dexJar") {
            dependsOn("jar")
            group = Mgpp.MindustryTaskGroup
            dependsOn(JavaPlugin.JAR_TASK_NAME)
            classpath.from(
                configurations.compileClasspath,
                configurations.runtimeClasspath
            )
            val jar = tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME)
            jarFiles.from(jar)
            sdkRoot.set(ex._deploy._androidSdkRoot)
        }
        tasks.register<Jar>("deploy") {
            group = Mgpp.MindustryTaskGroup
            val jar = tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME)
            dependsOn(jar)
            dependsOn(dexJar)
            destinationDirectory.set(temporaryDir)
            archiveBaseName.set(ex._deploy._baseName)
            archiveVersion.set(ex._deploy._version)
            archiveClassifier.set(ex._deploy._classifier)
            from(
                *jar.get().outputs.files.map { project.zipTree(it) }.toTypedArray(),
                *dexJar.get().outputs.files.map { project.zipTree(it) }.toTypedArray(),
            )
        }
        target.afterEvaluateThis {
            if (ex._deploy.enableFatJar.get()) {
                tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    from(
                        configurations.runtimeClasspath.get().map {
                            if (it.isDirectory) it else zipTree(it)
                        }
                    )
                }
            }
        }
        val modMeta = ex._modMeta.get()
        ex._deploy._baseName.convention(provider {
            modMeta.name
        })
        ex._deploy._version.convention(provider {
            modMeta.version
        })
    }
}
/**
 * Provides the existing [compileGroovy][org.gradle.api.tasks.compile.GroovyCompile] task.
 */
val TaskContainer.`dexJar`: TaskProvider<DexJar>
    get() = named<DexJar>("dexJar")
/**
 * Provides the existing [compileGroovy][org.gradle.api.tasks.compile.GroovyCompile] task.
 */
val TaskContainer.`deploy`: TaskProvider<Jar>
    get() = named<Jar>("deploy")

class MindustryAssetPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.func {
        val main = extensions.getOrCreate<MindustryExtension>(
            Mgpp.MainExtensionName
        )
        val assets = extensions.getOrCreate<MindustryAssetsExtension>(
            Mgpp.AssetExtensionName
        )
        val genModHjson = tasks.register<ModHjsonGenerate>("genModHjson") {
            group = Mgpp.MindustryTaskGroup
            modMeta.set(main._modMeta)
            outputHjson.set(temporaryDir.resolve("mod.hjson"))
        }
        tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
            dependsOn(genModHjson)
            from(genModHjson)
        }
        // Register this for dynamically configure tasks without class reference in groovy.
        // Eagerly configure this task in order to be added into task group in IDE
        tasks.register<AntiAlias>("antiAlias") {
            group = Mgpp.MindustryTaskGroup
        }.get()
        // Doesn't register the tasks if no resource needs to generate its class.
        val genResourceClass by lazy {
            tasks.register<RClassGenerate>("genResourceClass") {
                this.group = Mgpp.MindustryAssetTaskGroup
                val name = assets.qualifiedName.get()
                if (name == "default") {
                    val modMeta = main._modMeta.get()
                    val (packageName, _) = modMeta.main.packageAndClassName()
                    qualifiedName.set("$packageName.R")
                } else {
                    qualifiedName.set(name)
                }
            }
        }
        target.afterEvaluateThis {
            val assetsRoot = assets.assetsRoot.get()
            if (assetsRoot != Mgpp.DefaultEmptyFile) {
                tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
                    from(assetsRoot)
                }
            }
            val icon = assets.icon.get()
            tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
                from(icon)
            }
            // Resolve all batches
            val group2Batches = assets.batches.get().resolveBatches()
            val jar = tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME)
            var genResourceClassCounter = 0
            for ((type, batches) in group2Batches) {
                if (batches.isEmpty()) continue
                jar.configure {
                    batches.forEach { batch ->
                        val dir = batch.dir
                        val root = batch.root
                        if (root == Mgpp.DefaultEmptyFile) {
                            it.from(dir.parentFile) {
                                it.include("${dir.name}/**")
                            }
                        } else { // relative path
                            it.from(root) {
                                it.include("$dir/**")
                            }
                        }
                    }
                }
                if (!batches.any { it.enableGenClass }) continue
                val groupPascal = type.group.lowercase().capitalized()
                val gen = tasks.register<ResourceClassGenerate>("gen${groupPascal}Class") {
                    this.group = Mgpp.MindustryAssetTaskGroup
                    dependsOn(batches.flatMap { it.dependsOn }.distinct().toTypedArray())
                    args.put("ModName", main._modMeta.get().name)
                    args.put("ResourceNameRule", type.nameRule.name)
                    args.putAll(assets.args)
                    args.putAll(type.args)
                    generator.set(type.generator)
                    className.set(type.className)
                    resources.setFrom(batches.filter { it.enableGenClass }.map { it.dir })
                }
                genResourceClass.get().apply {
                    dependsOn(gen)
                    classFiles.from(gen)
                }
                genResourceClassCounter++
            }
            if (genResourceClassCounter > 0) {
                safeRun {
                    tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
                        it.dependsOn(genResourceClass)
                    }
                }
                safeRun {
                    tasks.named("compileKotlin") {
                        it.dependsOn(genResourceClass)
                    }
                }
                safeRun {
                    tasks.named("compileGroovy") {
                        it.dependsOn(genResourceClass)
                    }
                }
            }
        }
    }
}
/**
 * Provides the existing [compileGroovy][org.gradle.api.tasks.compile.GroovyCompile] task.
 */
val TaskContainer.`antiAlias`: TaskProvider<AntiAlias>
    get() = named<AntiAlias>("antiAlias")
/**
 * Provides the existing [compileGroovy][org.gradle.api.tasks.compile.GroovyCompile] task.
 */
val TaskContainer.`genModHjson`: TaskProvider<ModHjsonGenerate>
    get() = named<ModHjsonGenerate>("genModHjson")

inline fun safeRun(func: () -> Unit) {
    try {
        func()
    } catch (_: Throwable) {
    }
}
/**
 * For downloading and running game.
 */
class MindustryAppPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val ex = target.extensions.getOrCreate<MindustryExtension>(
            Mgpp.MainExtensionName
        )
        target.parent?.let {
            it.plugins.whenHas<MindustryPlugin> {
                val parentEx = it.extensions.getOrCreate<MindustryExtension>(Mgpp.MainExtensionName)
                ex._dependency.mindustryDependency.set(parentEx._dependency.mindustryDependency)
                ex._dependency.arcDependency.set(parentEx._dependency.arcDependency)
            }
        }
        val resolveMods = target.tasks.register<ResolveMods>(
            "resolveMods"
        ) {
            group = Mgpp.MindustryTaskGroup
            mods.set(ex._mods.worksWith)
        }
        target.afterEvaluateThis {
            // For client side
            val downloadClient = tasks.register<Download>(
                "downloadClient",
            ) {
                group = Mgpp.MindustryTaskGroup
                keepOthers.set(ex._client.keepOtherVersion)
                location.set(ex._client.location)
            }
            arrayOf(Any()).any { it == 1 }
            // For server side
            val downloadServer = tasks.register<Download>(
                "downloadServer",
            ) {
                group = Mgpp.MindustryTaskGroup
                keepOthers.set(ex._client.keepOtherVersion)
                location.set(ex._server.location)
            }
            val runClient = tasks.register<RunMindustry>("runClient") {
                group = Mgpp.MindustryTaskGroup
                dependsOn(downloadClient)
                val dataDirEx = ex._run._dataDir.get()
                dataDir.set(
                    if (dataDirEx.isNotBlank() && dataDirEx != "temp")
                        File(dataDirEx)
                    else if (dataDirEx == "temp")
                        temporaryDir.resolve("data")
                    else // Default data directory
                        resolveDefaultDataDir()
                )
                mindustryFile.setFrom(downloadClient)
                modsWorkWith.setFrom(resolveMods)
                dataModsPath.set("mods")
                ex._mods._extraModsFromTask.get().forEach {
                    outputtedMods.from(tasks.getByPath(it))
                }
            }
            val runServer = tasks.register<RunMindustry>(
                "runServer",
            ) {
                group = Mgpp.MindustryTaskGroup
                dependsOn(downloadServer)
                mainClass.convention(Mgpp.MindustrySeverMainClass)
                mindustryFile.setFrom(downloadServer)
                modsWorkWith.setFrom(resolveMods)
                dataModsPath.convention("config/mods")
                ex._mods._extraModsFromTask.get().forEach {
                    dependsOn(tasks.getByPath(it))
                    outputtedMods.from(tasks.getByPath(it))
                }
            }
        }
    }
}
/**
 * Provides the existing [compileGroovy][org.gradle.api.tasks.compile.GroovyCompile] task.
 */
val TaskContainer.`resolveMods`: TaskProvider<ResolveMods>
    get() = named<ResolveMods>("resolveMods")

fun Project.resolveDefaultDataDir(): File {
    return when (getOs()) {
        OS.Unknown -> {
            logger.warn("Can't recognize your operation system.")
            Mgpp.DefaultEmptyFile
        }
        OS.Windows -> FileAt(System.getenv("AppData"), "Mindustry")
        OS.Linux -> FileAt(System.getenv("XDG_DATA_HOME") ?: System.getenv("HOME"), ".local", "share", "Mindustry")
        OS.Mac -> FileAt(System.getenv("HOME"), "Library", "Application Support", "Mindustry")
    }
}