plugins {
    kotlin("jvm") version "2.2.21"
    application
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/MorpheApp/registry")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: "wowmancode"
            password = System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.MorpheApp.smali")
            includeGroup("com.github.REAndroid")
        }
    }
}

dependencies {
    implementation("app.morphe:morphe-patcher:1.9.0")
    implementation("com.github.MorpheApp.smali:smali:d856bad65f")
    implementation("com.github.REAndroid:arsclib:a28c6fb2a7")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-opt-in=app.morphe.patcher.InternalApi"
        )
    }
}

application {
    mainClass.set("runner.MainKt")
}
