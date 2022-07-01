package plumy.dsl

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File

typealias StringProp = Property<String>
typealias StringsProp = ListProperty<String>
typealias DirProp = DirectoryProperty
typealias FileProp = Property<File>