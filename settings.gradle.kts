pluginManagement {
    val kotlinVersion = "1.8.0"
    plugins {
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}


rootProject.name = "XenoECS"
