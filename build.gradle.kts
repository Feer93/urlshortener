import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.5.5" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    kotlin("jvm") version "1.5.31" apply false
    kotlin("plugin.spring") version "1.5.31" apply false
    kotlin("plugin.jpa") version "1.5.31" apply false
}

group = "es.unizar"
version = "0.0.1-SNAPSHOT"


subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    repositories {
        mavenCentral()
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        //"implementation"("org.springframework.boot:spring-boot-starter-actuator")
    }
}

project(":core") {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "implementation"("org.springframework.boot:spring-boot-starter-actuator")
        "implementation"("io.micrometer:micrometer-core")
        "implementation"("org.springframework.boot:spring-boot-starter-cache")
        "implementation"("io.micrometer:micrometer-registry-prometheus")
        "implementation"("com.google.zxing:core:3.4.1")
        "implementation"("com.google.zxing:javase:3.4.1")
        "implementation"("com.maxmind.geoip2:geoip2:2.15.0")
        //Testing
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.mockito.kotlin:mockito-kotlin:3.2.0")
    }
    tasks.getByName<BootJar>("bootJar") {
        enabled = false
    }
}

project(":repositories") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    dependencies {
        "implementation"(project(":core"))
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
    }
    tasks.getByName<BootJar>("bootJar") {
        enabled = false
    }
}

project(":delivery") {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    dependencies {
        "implementation"(project(":core"))
        "implementation"("org.springframework.boot:spring-boot-starter-webflux")
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-actuator")
        "implementation"("org.springframework.boot:spring-boot-starter-hateoas")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "implementation"("commons-validator:commons-validator:1.6")
        "implementation"("com.google.guava:guava:23.0")
        "implementation"("io.micrometer:micrometer-core")
        "implementation"("io.micrometer:micrometer-registry-prometheus")
        "implementation"("com.maxmind.geoip2:geoip2:2.15.0")
        "implementation"("org.quartz-scheduler:quartz")
        //Swagger and Open API dependencies
        "implementation" ("org.springdoc:springdoc-openapi-ui:1.5.2")
        "implementation" ("io.springfox:springfox-swagger2:2.9.2")
        "implementation" ("io.springfox:springfox-swagger-ui:2.9.2")
        "implementation"("io.springfox:springfox-boot-starter:3.0.0")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.mockito.kotlin:mockito-kotlin:3.2.0")
    }
    tasks.getByName<BootJar>("bootJar") {
        enabled = false
    }
}

project(":app") {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    dependencies {
        "implementation"(project(":core"))
        "implementation"(project(":delivery"))
        "implementation"(project(":repositories"))
        "implementation"("org.springframework.boot:spring-boot-starter-webflux")
        "implementation"("org.springframework.boot:spring-boot-starter")
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-actuator")
        "implementation"( "org.webjars:bootstrap:3.3.5")
        "implementation"("org.webjars:jquery:2.1.4")
        "implementation"("com.maxmind.geoip2:geoip2:2.15.0")
        "implementation"("org.quartz-scheduler:quartz")
        "implementation"("org.springframework.boot:spring-boot-starter-cache")
        "implementation"("com.google.guava:guava:23.0")

        "runtimeOnly"("org.hsqldb:hsqldb")

        //Swagger and Open API dependencies
        "implementation" ("org.springdoc:springdoc-openapi-ui:1.5.2")
        "implementation" ("io.springfox:springfox-swagger2:3.0.0")
        "implementation" ("io.springfox:springfox-swagger-ui:3.0.0")
        "implementation"("io.springfox:springfox-boot-starter:3.0.0")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.springframework.boot:spring-boot-starter-web")
        "testImplementation"("org.springframework.boot:spring-boot-starter-jdbc")
        "testImplementation"("org.mockito.kotlin:mockito-kotlin:3.2.0")
        "testImplementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "testImplementation"("org.apache.httpcomponents:httpclient")
    }
}
