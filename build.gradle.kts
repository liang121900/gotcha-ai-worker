plugins {
	id("org.jetbrains.kotlin.jvm") version "1.6.21"
	id("org.jetbrains.kotlin.kapt") version "1.6.21"
	id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("io.micronaut.application") version "3.6.3"
}

version = "0.1"
group = "com.gotcha.ai.worker"

val kotlinVersion = project.properties.get("kotlinVersion")
repositories {
	mavenCentral()
}

dependencies {
	implementation("io.micronaut:micronaut-jackson-databind")
	implementation("io.micronaut.aws:micronaut-aws-sdk-v2")
	implementation("io.micronaut:micronaut-management")
	runtimeOnly("io.micronaut:micronaut-http-server-netty")
	implementation("software.amazon.awssdk:sqs")
	implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
	implementation("io.micronaut.objectstorage:micronaut-object-storage-aws")
	implementation("jakarta.annotation:jakarta.annotation-api")
	implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
	implementation("software.amazon.awssdk:dynamodb-enhanced:2.18.28")
	runtimeOnly("ch.qos.logback:logback-classic")
	implementation("io.micronaut:micronaut-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
	implementation("io.projectreactor:reactor-core")
	testImplementation("io.micronaut:micronaut-http-client")
	// For running external process
	implementation("com.github.pgreze:kotlin-process:1.4")
	// For supporting coroutines and reactor together with Kotlin
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
	// For S3
	implementation("io.micronaut.objectstorage:micronaut-object-storage-aws:1.1.0")
	// For mapstruct
	implementation("org.mapstruct:mapstruct:1.5.3.Final")
	kapt("org.mapstruct:mapstruct-processor:1.5.3.Final")

}


application {
	mainClass.set("com.gotcha.ai.worker.ApplicationKt")
	// Run as local in default
	applicationDefaultJvmArgs = listOf("-Dmicronaut.environments=local")
}
java {
	sourceCompatibility = JavaVersion.toVersion("17")
}

tasks {
	compileKotlin {
		kotlinOptions {
			jvmTarget = "17"
		}
	}
	compileTestKotlin {
		kotlinOptions {
			jvmTarget = "17"
		}
	}

	dockerBuild {
		/*inputDir.set(project.projectDir)*/
		dockerFile.set(file("${buildDir}/docker/main/Dockerfile"))
	}
}



micronaut {
	testRuntime("junit5")
	processing {
		incremental(true)
		annotations("com.gotcha.ai.worker.*")
	}
}

kapt {
	arguments {
		// Set Mapstruct Configuration options here
		// https://kotlinlang.org/docs/reference/kapt.html#annotation-processor-arguments
		// https://mapstruct.org/documentation/stable/reference/html/#configuration-options
		arg("mapstruct.defaultComponentModel", "jsr330")
	}

}
