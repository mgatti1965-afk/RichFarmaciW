import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.aistudio.richfarmaciw.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RichFarmaci"
            packageVersion = "1.0.0"
            description = "RichFarmaci - Gestione Farmaci e Prescrizioni"
            copyright = "© 2025 RichFarmaci"
            vendor = "RichFarmaci"

            windows {
                menu = true
                shortcut = true
                dirChooser = true
                menuGroup = "RichFarmaci"
            }
        }
    }
}
