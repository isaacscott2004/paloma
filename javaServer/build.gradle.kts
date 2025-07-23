plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.paloma.paloma"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations.named("compileOnly") {
	extendsFrom(configurations.getByName("annotationProcessor"))
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(libs.spring.boot.starter.oauth2.authorization.server)
	implementation(libs.spring.boot.starter.oauth2.client)
	compileOnly(libs.lombok)
	developmentOnly(libs.spring.boot.devtools)
	runtimeOnly(libs.mysql.connector.j)
	annotationProcessor(libs.projectlombok.lombok)
	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.spring.security.test)
	testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
	useJUnitPlatform()
}
