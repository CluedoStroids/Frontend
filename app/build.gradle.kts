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

    buildTypes {
        release {
            isMinifyEnabled = false
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

    testOptions {
        unitTests {
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
    implementation(libs.androidx.core.ktx.v1131)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout.v214)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx.v280)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.core.v173)
    implementation(libs.kotlinx.coroutines.android.v173)
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.gson)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.androidx.navigation.runtime.android)
    // JUnit 4
    testImplementation(libs.junit)
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2") // For JUnit 4 compatibility
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
}