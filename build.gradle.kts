plugins {
    id ("java")
    id ("war")
    id ("cern.devops.cbng.dependency")
    id ("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
}

configurations.configureEach {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

val springBootVersion = org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES.substringAfterLast(':')
val log4j2Version = dependencyManagement.importedProperties["log4j2.version"]

dependencies {
    implementation ("cern.japc:japc:PRO")
    implementation ("cern.japc:japc-ext-cmwrda3:PRO")
    implementation ("cern.japc:japc-value:PRO")
    implementation ("cern.japc:japc-ext-remote:PRO")
    implementation ("cern.accsoft.security:rbac-client:PRO")
    implementation ("cern.accsoft.security:rbac-common:PRO")
    implementation ("cern.accsoft.security:rbac-util:PRO")
    implementation ("cern.cmw:cmw-data:PRO")
    implementation ("cern.cmw:cmw-rda3:PRO")

    implementation ("cern.rade.services:codec:refactor-SNAPSHOT")
    implementation ("cern.rade.services:common:refactor-SNAPSHOT")

    implementation ("org.apache.commons:commons-lang3:3.12.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    //implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    //implementation("org.springframework.boot:spring-boot-starter-logging:3.1.2")



    //necessary for log4j2 yaml config file to work

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    //log4j2 from springboot
    implementation("org.springframework.boot:spring-boot-starter-log4j2:$springBootVersion")

    //needed to use json layouts for log messages
    implementation("org.apache.logging.log4j:log4j-layout-template-json:$log4j2Version")

    implementation ("org.springframework.boot:spring-boot-starter-websocket:$springBootVersion"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
    implementation ("org.springframework.boot:spring-boot-starter-webservices:$springBootVersion"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    implementation ("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat-runtime:$springBootVersion")
}


//    implementation(files("/home/ncass/Workspace/common/build/libs/common-1.0.3.jar"))
////    implementation(files("/home/ncass/Workspace/rade-codec/java-codec/build/libs/codec-1.0.3.jar"))
//    implementation(files("C:\\Users\\ncass\\workspace\\common\\build\\libs\\common-test.jar"))
//    implementation(files("C:\\Users\\ncass\\workspace\\rade-codec\\java-codec\\build\\libs\\codec-1.0.3.jar"))
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
    val warName = project.findProperty("warName")
    if(warName!=null){
        archiveFileName.set(warName as String)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
// requirement for building this project
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}
// missing: manifest (see https://docs.gradle.org/7.4.2/userguide/building_java_projects.html#sec:jar_manifest)

