import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
}

compose.resources {
    publicResClass = true
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {

    val isMac = System.getProperty("os.name").contains("Mac", ignoreCase = true)

    if (isMac) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "Shared"
                isStatic = true
            }
        }
    }
    
    jvm()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    js {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.materialIconsExtended)
                implementation(libs.compose.ui)
                api(libs.compose.components.resources)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                api(libs.androidx.room.common)
            }
        }

        val roomTargetsMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.androidx.room.runtime)
                api(libs.sqlite.bundled)
                api(libs.sqlite)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidMain by getting {
            dependsOn(roomTargetsMain)
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.compose.uiTooling)
                implementation(libs.ktor.client.cio)
            }
        }

        val jvmMain by getting {
            dependsOn(roomTargetsMain)
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        if (isMac) {
            val iosMain by getting {
                dependsOn(roomTargetsMain)
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
        }

        val webMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.js)
            }
        }

        val wasmJsMain by getting {
            dependsOn(webMain)
        }

        val jsMain by getting {
            dependsOn(webMain)
        }

        val webTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        val wasmJsTest by getting {
            dependsOn(webTest)
        }

        val jsTest by getting {
            dependsOn(webTest)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
        add("kspIosX64", libs.androidx.room.compiler)
        add("kspIosArm64", libs.androidx.room.compiler)
        add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    }
}

android {
    namespace = "com.aistudio.richfarmaciw.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
