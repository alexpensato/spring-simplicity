import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "2.2.6.RELEASE"
val springVersion = "5.2.5.RELEASE"
val jacksonVersion = "2.10.3"
val h2Version = "1.4.200"

plugins {
    `java-library`

    // https://github.com/xvik/gradle-java-lib-plugin
    // id("ru.vyarus.java-lib") version "2.1.0"

    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
}

group = "org.pensatocode.simplicity"
version = "0.7.0"
description = "A tiny framework for building fast and reliable RESTful APIs in Kotlin."
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework:spring-web:${springVersion}")
    implementation("org.springframework:spring-beans:${springVersion}")
    implementation("org.springframework:spring-context:${springVersion}")
    implementation("org.springframework:spring-jdbc:${springVersion}")
    implementation("org.springframework.data:spring-data-commons:${springBootVersion}")

    // Kotlin language
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Database
//    implementation("postgresql:postgresql:9.1-901-1.jdbc4")
    implementation("com.h2database:h2:${h2Version}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${springBootVersion}") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

// https://guides.gradle.org/building-kotlin-jvm-libraries/
tasks {
    jar {
        manifest {
            attributes(
                    mapOf("Implementation-Title" to project.name,
                            "Implementation-Version" to project.version)
            )
        }
    }

    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        val sourceSets: SourceSetContainer by project;
        from(sourceSets["main"].allSource);
    }

    artifacts {
        add("archives", sourcesJar)
    }
}