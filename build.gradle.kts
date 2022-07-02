import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }
}

plugins {
    kotlin("jvm")
    groovy
    `java-gradle-plugin`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.18.0"
}
group = "io.github.liplum.mgpp"
val mgppVersion: String by project
version = mgppVersion
repositories {
    mavenCentral()
    maven {
        url = uri("https://www.jitpack.io")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        excludeTags("slow")
    }
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}
gradlePlugin {
    plugins {
        create("plumyMindustryPlugin") {
            id = "io.github.liplum.mgpp"
            displayName = "mgpp"
            description = "For Mindustry modding in Java, kotlin and so on."
            implementationClass = "io.github.liplum.mindustry.MindustryPlugin"
        }
    }
}
pluginBundle {
    website = "https://plumygame.github.io/mgpp/"
    vcsUrl = "https://github.com/PlumyGame/mgpp"
    tags = listOf("mindustry", "mindustry-mod", "mod")
}
tasks.named<GroovyCompile>("compileGroovy") {
    val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")
    dependsOn(compileKotlin)
    classpath += files(compileKotlin.get().destinationDirectory)
}
tasks.named<GroovyCompile>("compileTestGroovy") {
    val compileTestKotlin = tasks.named<KotlinCompile>("compileTestKotlin")
    dependsOn(compileTestKotlin)
    classpath += files(compileTestKotlin.get().destinationDirectory)
}
val pluginName: String by project
sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDir("resources")
        groovy.srcDir("src")
    }
    test {
        java.srcDir("test")
        resources.srcDir("resources")
        groovy.srcDir("test")
    }
}
repositories {
    mavenCentral()
}
val arcVersion: String by project
dependencies {
    implementation("com.github.anuken.arc:arc-core:$arcVersion")
    implementation("org.hjson:hjson:3.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<Jar>("jar") {
    archiveBaseName.set(pluginName)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    includeEmptyDirs = false
    from(
        configurations.runtimeClasspath.get().mapNotNull {
            if (it.isFile && it.extension == "jar"
                && ("arc-core" in it.name || "hjson" in it.name))
                zipTree(it)
            else null
        }
    )
    from(sourceSets.main.get().allSource)
}
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}