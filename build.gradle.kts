plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.3"
    id("com.google.protobuf") version "0.9.6"
}
val springGrpcVersion by extra("1.0.0")

group = "online.eracodes"
version = "0.0.1"
description = "Secure Spring Boot service for enrolling entities with signed responses."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.2"
        // To use local executable
        // path = "src/main/resources/proto"
    }

    plugins {
        // grpc { artifact = "io.grpc:protoc-gen-grpc-java" }
    }

    generateProtoTasks {
        // all()*.plugins {
        //     grpc {
        //         option '@generated=omit'
        //     }
        // }
        // all().each { task ->
        //     task.builtins {
        //         java {
        //             option 'annotate_code'
        //         }
        //     }
        // }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    implementation("com.google.protobuf:protobuf-java:4.28.2")
    implementation("com.google.protobuf:protobuf-java-util:3.25.1")
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")

    implementation("org.bouncycastle:bcprov-jdk18on:1.83")
    implementation("org.bouncycastle:bcpqc-jdk18on:1.83")

    // For PGP or CMS functionality
    // implementation 'org.bouncycastle:bcpg-jdk18on:1.81'

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    runtimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:$springGrpcVersion")
    }
}

protobuf {
    protoc {
        // Option 1: Use artifact (automatically downloads protoc for your OS)
        artifact = "com.google.protobuf:protoc:4.28.2"

        // Option 2: Use local protoc executable (uncomment and set path if needed)
        // path = "path/to/protoc"
    }
    generateProtoTasks {
        ofSourceSet("main")
        ofSourceSet("test")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
