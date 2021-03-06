import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.21"
}

group = "clockvapor.telegram"
version = "0.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.github.clockvapor", "markov", "0.0.0")
    compile("com.github.clockvapor", "telegram-utils", "0.0.1")
    compile("org.twitter4j", "twitter4j-core", "4.0.7")
    compile("com.xenomachina", "kotlin-argparser", "2.0.7")
    compile("io.github.seik", "kotlin-telegram-bot", "0.3.5") {
        exclude("io.github.seik.kotlin-telegram-bot", "echo")
        exclude("io.github.seik.kotlin-telegram-bot", "dispatcher")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

task<Jar>("fatJar") {
    manifest {
        attributes(mapOf("Main-Class" to "clockvapor.telegram.twittermarkov.TwitterMarkovTelegramBot"))
    }
    from(configurations.runtimeClasspath
        .filter { it.exists() }
        .map { if (it.isDirectory) it else zipTree(it) }
    )
    with(tasks["jar"] as CopySpec)
}

task("stage") {
    dependsOn("clean", "fatJar")
}
