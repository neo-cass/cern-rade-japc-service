plugins {
    id ("java")
    id ("war")
    id ("cern.devops.cbng.dependency")
    id ("cern.devops.cbng.project")
    id ("cern.devops.cbng.publication")
    id ("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
}

configurations.configureEach {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

extra["junit-jupiter.version"] = "5.11.0"

val springBootVersion =org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES.substringAfterLast(':')

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

    implementation ("cern.rade.services:codec:1.0.0")
    implementation ("cern.rade.services:common:1.0.0")

    implementation ("org.apache.commons:commons-lang3:3.12.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    //implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    //implementation("org.springframework.boot:spring-boot-starter-logging:3.1.2")



    //necessary for log4j2 yaml config file to work

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    //log4j2 from springboot
    implementation("org.springframework.boot:spring-boot-starter-log4j2:$springBootVersion")


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
    // OTLP export of metrics and logs to monit-otlp. Explicit versions: CBNG would otherwise substitute PRO.
    // Appender 2.28.x matches the OpenTelemetry SDK 1.62 that Spring Boot manages.
    implementation ("org.springframework.boot:spring-boot-starter-opentelemetry:$springBootVersion"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    implementation("io.opentelemetry.instrumentation:opentelemetry-log4j-appender-2.17:2.28.0-alpha")
    implementation("com.bucket4j:bucket4j_jdk17-core:8.14.0")
    implementation ("org.springframework.boot:spring-boot-starter-security:$springBootVersion"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    implementation ("org.springframework.boot:spring-boot-starter-oauth2-client:$springBootVersion"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat-runtime:$springBootVersion")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}


//    implementation(files("/home/ncass/Workspace/common/build/libs/common-1.0.3.jar"))
////    implementation(files("/home/ncass/Workspace/rade-codec/java-codec/build/libs/codec-1.0.3.jar"))
//    implementation(files("C:\\Users\\ncass\\workspace\\common\\build\\libs\\common-test.jar"))
//    implementation(files("C:\\Users\\ncass\\workspace\\rade-codec\\java-codec\\build\\libs\\codec-1.0.3.jar"))
springBoot {
    buildInfo()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
    val warName = project.findProperty("warName")
    if(warName!=null){
        archiveFileName.set(warName as String)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.processResources {
    from(layout.buildDirectory.dir("api-docs")) {
        into("api-docs")
    }
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


// Publication (cern.devops.cbng.publication): the artifact released to the Pro repository is the executable Spring Boot
// war produced by bootWar (cern.rade.services:japc:<version>@war), not the plain jar CBNG publishes by default.
// Repositories (Pro, Development, ...) and their credentials are the ones CBNG configures; none are defined here.
publishing {
    publications.named<MavenPublication>("cbng") {
        // CBNG creates this publication from the java component (plain jar + sources + javadoc): replace those with the boot war.
        // Maven file name is <artifactId>-<version>.war whatever -PwarName (image build) set on the file produced by bootWar.
        setArtifacts(emptyList<Any>())
        artifact(tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar"))
        pom {
            packaging = "war"
        }
    }
}
