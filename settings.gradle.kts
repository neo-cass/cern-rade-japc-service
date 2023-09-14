pluginManagement {
    repositories {
        // the CBNG plugin repo
        maven {url = uri ("https://artifactory.cern.ch/release")}
        maven {url = uri ("https://artifactory.cern.ch/gradle") }
        mavenCentral()
    }
}

rootProject.name = "japc"
