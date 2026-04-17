plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    // Bawaan Spring Boot & Kotlin
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // 1. WAJIB untuk membuat Endpoint API (@RestController, dll)
    implementation("org.springframework.boot:spring-boot-starter-web") 
    
    // 2. WAJIB untuk Database dan MySQL (@Entity, JpaRepository)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")

    // 3. WAJIB untuk Integrasi AI Gemini (WebClient)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // 4. WAJIB untuk membaca hasil JSON dari AI ke Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // 5. WAJIB UNTUK FITUR LOGIN & JWT (Ini yang tadi kurang)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Bawaan untuk Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
