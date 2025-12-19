plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.3"
    id("com.google.protobuf") version "0.9.6"
}

group = "online.eracodes"
version = "0.0.1"
description = "secure-enrollment-service"

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

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
