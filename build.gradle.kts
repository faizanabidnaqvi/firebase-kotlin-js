plugins {
    kotlin("js") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.0"
    `maven-publish`
}
group = "com.fz.firebase-kotlin-js"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    val serializationVersion = "1.0.0-RC"

    //serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serializationVersion")

    //firebase npm
    implementation(npm("firebase","7.20.0"))
}

kotlin {
    js {
        browser {
            useCommonJs()
        }
        binaries.executable()
    }
}

publishing {
    publications {
        create<MavenPublication>("firebase-kotlin-js") {

            from(components["kotlin"])
        }
    }
    repositories {
        maven("${project.rootDir}/releases")
    }
}