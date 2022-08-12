plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("org.jetbrains.kotlinx.benchmark") version "0.4.2"
    kotlin("plugin.allopen") version "1.6.0"
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

group = "com.xenotactic.ecs"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}


benchmark {
    configurations {
        named("main") {
//            iterations = 10
//            iterationTime = 5
//            iterationTimeUnit = "sec"
            outputTimeUnit = "ms"
        }
//        main { // main configuration is created automatically, but you can change its defaults
//            warmups = 20 // number of warmup iterations
//            iterations = 10 // number of iterations
//            iterationTime = 3 // time in seconds per iteration
//        }
//        smoke {
//            warmups = 5 // number of warmup iterations
//            iterations = 3 // number of iterations
//            iterationTime = 500 // time in seconds per iteration
//            iterationTimeUnit = "ms" // time unit for iterationTime, default is seconds
//        }
    }
    targets {
        register("main")
        register("jvm")
        register("jsIr")
    }
}