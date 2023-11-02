import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    id("com.revolut.jooq-docker") version "0.3.9"
}

group = "com.account_balancer"

repositories {
    mavenCentral()
}

dependencies {
//    spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

//    db
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.flywaydb:flyway-core")
    jdbc("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:postgresql")

//    serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

//    jwt
    implementation("io.jsonwebtoken:jjwt:0.12.3")

//  logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")

//  swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.2.0")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

//    coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.3")

//    test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.7.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.3")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.0"))
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks {
    generateJooqClasses {
        basePackageName = "com.account_balancer.db"
        outputSchemaToDefault = setOf("public")
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xjsr305=strict"
        }
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }

    clean {
        dependsOn("cleanGenerateJooqClasses")
    }
}
