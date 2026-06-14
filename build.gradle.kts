plugins {
	kotlin("jvm") version "2.4.0"
	kotlin("plugin.serialization") version "2.4.0"
	id("com.gradleup.shadow") version "9.4.1"
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://jitpack.io")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
	compileOnly("net.mamoe.yamlkt:yamlkt:0.13.0")
	compileOnly("com.github.Fusezion:Kotlin4Bukkit:1.0.0")
	implementation("com.github.Fusezion:ConfigSerialization:1.1.2")
}

kotlin {
	jvmToolchain(21)
}

java {
	withSourcesJar()
	withJavadocJar()
}

tasks {

	shadowJar {
		archiveClassifier = ""
		relocate("dev.lyric.config", "dev.lyric.income.sell.libs.config")
	}

	build {
		dependsOn(shadowJar)
	}

	processResources {
		val props = mapOf("version" to version)
		filesMatching("paper-plugin.yml") {
			expand(props)
		}
	}
}
