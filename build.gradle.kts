plugins {
    kotlin("js") //version "1.4.10"
    kotlin("plugin.serialization") //version "1.4.0"
}
group = "me.fz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val serializationVersion = "1.0.0-RC"

    //serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serializationVersion")

    //firebase npm
//    implementation(npm("firebase","7.20.0"))
}

kotlin {
    js {
        browser {
            
        }
    }
}