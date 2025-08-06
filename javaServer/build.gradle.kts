plugins {
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	java
}

group = "com.paloma.paloma"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.data.jpa)
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	developmentOnly(libs.boot.spring.boot.devtools)
	compileOnly(libs.org.projectlombok.lombok)
	annotationProcessor(libs.org.projectlombok.lombok2)
	testImplementation(libs.springframework.spring.boot.starter.test)
	implementation(libs.mysql.connector.java)
	implementation(libs.boot.spring.boot.starter.data.jpa)
	implementation(libs.spring.boot.starter.validation)
	implementation(libs.spring.boot.starter.security)
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")
	
	// Email dependencies
	implementation("org.springframework.boot:spring-boot-starter-mail") }
tasks.test {
	useJUnitPlatform()
}
