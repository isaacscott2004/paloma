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
	developmentOnly(libs.boot.spring.boot.devtools)
	compileOnly(libs.org.projectlombok.lombok)
	annotationProcessor(libs.org.projectlombok.lombok2)
	testImplementation(libs.springframework.spring.boot.starter.test)
	implementation("mysql:mysql-connector-java:8.0.33")}
