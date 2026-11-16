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

extra["junit-jupiter.version"] = "5.11.0"

val springBootVersion =org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES.substringAfterLast(':')

dependencies {
    // CERN dependencies are pinned for reproducible builds: update them deliberately, together, to the current PRO versions listed by CBNG.
    implementation ("cern.japc:japc:8.2.1")
    implementation ("cern.japc:japc-ext-cmwrda3:8.2.1")
    implementation ("cern.japc:japc-value:8.2.1")
    implementation ("cern.japc:japc-ext-remote:8.2.2")
    implementation ("cern.accsoft.security:rbac-client:10.11.1")
    implementation ("cern.accsoft.security:rbac-common:10.11.1")
    implementation ("cern.accsoft.security:rbac-util:10.11.1")
    implementation ("cern.cmw:cmw-data:10.11.2")
    implementation ("cern.cmw:cmw-rda3:10.11.2")

    implementation ("cern.rade.services:codec:1.0.1")
    implementation ("cern.rade.services:common:1.0.0")

    //necessary for log4j2 yaml config file to work

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    //log4j2 from springboot
    implementation("org.springframework.boot:spring-boot-starter-log4j2:$springBootVersion")


    implementation ("org.springframework.boot:spring-boot-starter-websocket:$springBootVersion"){
        exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
    }
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
    implementation ("org.springframework.boot:spring-boot-starter-webmvc:$springBootVersion"){
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
    testImplementation ("com.google.code.gson:gson:2.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}


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
