package io.github.liplum.dsl

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.provider.*
import java.io.File

internal
fun Task.deleteTempDir() {
    project.delete(temporaryDir.listFiles())
}
internal
fun Task.tempFi(name: String) =
    temporaryDir.resolve(name)
internal
fun Project.proDir(name: String) =
    projectDir.resolve(name)
internal
fun Project.rootDir(name: String) =
    rootDir.resolve(name)
internal inline
fun <reified T> ExtensionContainer.getOrCreate(
    extensionName: String,
): T {
    return findByType(T::class.java) ?: create(extensionName, T::class.java)
}
internal
fun Project.stringProp(): StringProp =
    objects.property(String::class.java)
internal
fun Project.boolProp(): BoolProp =
    objects.property(Boolean::class.java)
internal
fun Project.stringsProp(): StringsProp =
    objects.listProperty(String::class.java)
internal inline
fun <reified T> Project.listProp(): ListProperty<T> =
    objects.listProperty(T::class.java)
internal inline
fun <reified TK, reified TV> Project.mapProp(): MapProperty<TK, TV> =
    project.objects.mapProperty(TK::class.java, TV::class.java)
internal inline
fun <reified T> Project.setProp(): SetProperty<T> =
    objects.setProperty(T::class.java)
internal inline
fun <reified T> Project.prop(): Property<T> =
    objects.property(T::class.java)
internal
fun Project.dirProp(): DirProp =
    objects.directoryProperty()
internal
fun Project.fileProp(): FileProp =
    objects.property(File::class.java)
internal
fun Project.configurationFileCollection(): ConfigurableFileCollection =
    objects.fileCollection()
internal inline
fun <reified T> T.func(func: T.() -> Unit) {
    this.func()
}
internal inline
fun <reified T : Plugin<*>> PluginContainer.apply(
): T = this.apply(T::class.java)
internal inline
fun <reified T : Plugin<*>> PluginContainer.whenHas(
    func: () -> Unit,
) {
    if (hasPlugin(T::class.java)) func()
}
internal inline
fun PluginContainer.whenHas(
    pluginID: String,
    func: () -> Unit,
) {
    if (hasPlugin(pluginID)) func()
}
internal
fun Project.dirProv(file: File): Provider<Directory> {
    return layout.dir(provider { file })
}
internal inline
fun Project.dirProv(crossinline prov: () -> File): Provider<Directory> {
    return layout.dir(provider { prov() })
}

inline fun <reified T> Project.new(): T =
    objects.newInstance(T::class.java)

inline fun <reified T> DefaultTask.new(): T =
    project.new()
internal
fun Project.afterEvaluateThis(func: Project.() -> Unit) {
    afterEvaluate(func)
}