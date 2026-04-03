plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
android {
    namespace = "dev.jmoicano.multiplayer.feature.search"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:player"))
    implementation(project(":core:designsystem"))
    // ...existing code...
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    // Paging
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    // Testing
    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test)
}
