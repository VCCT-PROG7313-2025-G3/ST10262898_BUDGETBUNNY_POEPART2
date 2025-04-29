plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    //This is the import/plugin that is needed for Kapt
    id("org.jetbrains.kotlin.kapt")


}

android {
    namespace = "com.fake.st10262898_budgetbunny_poepart2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fake.st10262898_budgetbunny_poepart2"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding {
        enable = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //These are the dependencies needed in order for RoomDb to run:
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.lifecycle.viewmodel.ktx)

    implementation("androidx.gridlayout:gridlayout:1.0.0") //Added for grid layouts in transaction page
}