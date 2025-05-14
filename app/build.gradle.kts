plugins {
    id("kotlin-kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("jacoco")
    id("org.sonarqube") version "5.1.0.4882"
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testApplicationId = "com.example.myapplication.test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources.excludes.add("META-INF/androidx.cardview_cardview.version")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = false
        viewBinding = true
    }

    packaging {
        resources {
            // Exclude duplicate files from the build
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/LICENSE-notice.md")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")

            // Handle duplicate R classes
            pickFirsts.add("**/R.class")
            pickFirsts.add("**/R$*.class")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true

            all {
                it.useJUnitPlatform()
                it.finalizedBy(tasks.named("jacocoTestReport"))
            }
        }
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates code coverage report for the test task."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${project.projectDir}/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )

    val debugTree =
        fileTree("${project.layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
            exclude(fileFilter)
        }

    val javaDebugTree =
        fileTree("${project.layout.buildDirectory.get().asFile}/intermediates/javac/debug") {
            exclude(fileFilter)
        }

    val mainSrc = listOf(
        "${project.projectDir}/src/main/java",
        "${project.projectDir}/src/main/kotlin"
    )

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree, javaDebugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get().asFile) {
        include("jacoco/testDebugUnitTest.exec")
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

sonar {
    properties {
        property("sonar.projectKey", "CluedoStroids_Frontend")
        property("sonar.organization", "cluedostroids")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
subprojects {
    sonar {
        properties {
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                "${project.projectDir}/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
            )
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                "build/reports/kover/reportKarosBeta.xml"
            )

        }
    }
}

dependencies {
    implementation(libs.androidx.activity)
    val naikSoftwareVersion = "1.6.6"

    implementation(libs.androidx.core.ktx.v1131)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout.v214)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx.v280)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.core.v173)
    implementation(libs.kotlinx.coroutines.android.v173)
    implementation("com.github.NaikSoftware:StompProtocolAndroid:$naikSoftwareVersion")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.gson)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.androidx.navigation.runtime.android)
    //implementation(libs.cardview.v7)
    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.mockito.inline)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.robolectric.robolectric)
    testImplementation(libs.turbine)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api.v5122)
    testRuntimeOnly(libs.junit.jupiter.engine.v5122)
    testRuntimeOnly(libs.junit.vintage.engine.v5122)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.powermock.module.junit4)
    testImplementation(libs.powermock.api.mockito2)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)

}