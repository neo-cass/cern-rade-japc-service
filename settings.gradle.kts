pluginManagement {
    val cbng_version: String by settings
    repositories {
        maven { url = uri("https://acc-repo-prod.cern.ch/repository/gradle") }
        maven { url = uri("https://acc-repo-prod.cern.ch/repository/release") }
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("cern.devops.cbng.dependency") version cbng_version
        id("cern.devops.cbng.project") version cbng_version
        id("cern.devops.cbng.publication") version cbng_version
    }
}

rootProject.name = "japc"
