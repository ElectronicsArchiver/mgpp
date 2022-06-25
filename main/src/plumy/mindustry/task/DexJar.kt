package plumy.mindustry.task

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.*
import plumy.dsl.*
import java.io.File

open class DexJar : DefaultTask() {
    val jarFiles = project.configurationFileCollection()
        @InputFiles get
    val classpath = project.configurationFileCollection()
        @InputFiles get
    val sdkRoot = project.stringProp()
        @Input @Optional get
    val workingDir = project.fileProp()
        @Optional @Input get
    val dexedJar = project.fileProp()
        @OutputFile get

    init {
        dexedJar.convention(temporaryDir.resolve("dexed.jar"))
        workingDir.convention(temporaryDir)
        sdkRoot.convention(System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT") ?: "")
    }
    @TaskAction
    fun dex() {
        val sdkPath = sdkRoot.get()
        val sdkRootDir = File(sdkPath)
        if (!sdkRootDir.exists()) throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
        val androidJarFile = run {
            // searching for the `android.jar` in Android SDK Path
            (sdkRootDir.resolve("platforms").listFiles() ?: emptyArray()).sorted().reversed()
                .platformFindAndroidJar()
                ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")
        }
        var d8 = "d8"
        // Check the default d8 command
        try {
            project.exec {
                it.commandLine = listOf(d8)
            }
        } catch (_: Exception) {
            logger.info("d8 isn't available on your platform, the absolute path of d8 will be found and utilized.")
            val d8File = run {
                // searching for the `android.jar` in Android SDK Path
                (sdkRootDir.resolve("build-tools").listFiles() ?: emptyArray()).sorted().reversed()
                    .platformFindD8()
                    ?: throw GradleException("No d8 found. Ensure that you have an Android build-tools installed.")
            }
            d8 = d8File.absolutePath
        }
        val dexedJarFile = dexedJar.get()
        dexedJarFile.parentFile.mkdirs()
        val classpaths = classpath.files + androidJarFile
        val jars = jarFiles.files
        val params = ArrayList<String>(classpaths.size * 2 + jars.size + 5)
        params.add(d8)
        for (classpath in classpaths) {
            params.add("--classpath")
            params.add(classpath.path)
        }
        params.add("--min-api")
        params.add("14")
        params.add("--output")
        params.add(dexedJarFile.absolutePath)
        params.addAll(jars.map { it.absolutePath })
        project.exec {
            it.commandLine = params
            it.workingDir = workingDir.get()
            it.standardOutput = System.out
            it.errorOutput = System.err
        }
    }
}

fun List<File>.platformFindD8(): File? =
    when (getOs()) {
        OS.Windows -> find { File(it, "d8.bat").exists() }?.run { File(this, "d8.bat") }
        OS.Linux -> find { File(it, "d8").exists() }?.run { File(this, "d8") }
        OS.Mac -> find { File(it, "d8").exists() }?.run { File(this, "d8") }
        else -> null
    }

fun List<File>.platformFindAndroidJar(): File? =
    find { File(it, "android.jar").exists() }?.run { File(this, "android.jar") }