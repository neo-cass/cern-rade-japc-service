plugins {
    id ("java")
    id ("war")
    id ("cern.devops.cbng.dependency") version "4.0.83"
    id ("org.springframework.boot") version "3.1.3"


    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"
}

repositories {
    // our package index to resolve dependencies
    maven {
        url = uri("https://artifactory.cern.ch/release")
    }
    mavenCentral()
}

configurations.configureEach {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    //TODO: get rid of PRO
    implementation ("cern.japc:japc:PRO")
    implementation ("cern.japc:japc-ext-cmwrda3:PRO")
    implementation ("cern.japc:japc-value:PRO")
    implementation ("cern.japc:japc-ext-remote:PRO")
    implementation ("cern.accsoft.security:rbac-client:PRO")
    implementation ("cern.accsoft.security:rbac-common:PRO")
    implementation ("cern.accsoft.security:rbac-util:PRO")
    implementation ("cern.cmw:cmw-data:PRO")

    implementation ("cern.rade.services:codec:master")
    implementation ("cern.rade.services:common:master")

    implementation ("org.apache.commons:commons-lang3:3.12.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    //implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    //implementation("org.springframework.boot:spring-boot-starter-logging:3.1.2")



    //necessary for log4j2 yaml config file to work
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")

    implementation("org.springframework.boot:spring-boot-starter-log4j2:3.1.3")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:2.20.0")

    implementation("org.apache.logging.log4j:log4j-spring-boot:2.20.0"){
        exclude(group="commons-logging", module="commons-logging")
    }
    implementation ("org.springframework.boot:spring-boot-starter-websocket:3.1.3"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.1.3")
    implementation ("org.springframework.boot:spring-boot-starter-web-services:3.1.3"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat:3.1.3")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
    val warName = project.findProperty("warName")
    if(warName!=null){
        archiveFileName.set(warName as String)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
// requirement for building this project
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withJavadocJar()
    withSourcesJar()
}
// missing: manifest (see https://docs.gradle.org/7.4.2/userguide/building_java_projects.html#sec:jar_manifest)

