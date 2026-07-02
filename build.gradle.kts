plugins {
	kotlin("jvm") version "2.4.0"
	kotlin("plugin.serialization") version "2.4.0"
	id("com.gradleup.shadow") version "9.4.1"
	id("xyz.jpenilla.run-paper") version "3.0.2"
	id("maven-publish")
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://jitpack.io")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
	compileOnly("com.github.Fusezion:IncomeEconomy:1.1.2")
	implementation(kotlin("stdlib"))
	implementation("dev.jorel:commandapi-paper-shade:11.2.0")
	implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
	implementation("com.github.Fusezion:ConfigSerialization:1.1.2")
	implementation("dev.triumphteam:triumph-gui:3.1.13")
}

kotlin {
	jvmToolchain(25)
}

java {
	withSourcesJar()
	withJavadocJar()
}

tasks {

	shadowJar {
		archiveClassifier = ""
		relocate("dev.lyric.config", "dev.lyric.income.sell.libs.config")
		relocate("dev.triumphteam.gui", "dev.lyric.income.sell.libs.gui")
		relocate("dev.jorel.commandapi", "dev.lyric.income.sell.libs.commandapi")
	}

	build {
		dependsOn(shadowJar)
	}

	runServer {
		downloadPlugins {
			modrinth("skript", "2.15.3")
			modrinth("excellenteconomy", "2.8.0")
			modrinth("nightcore", "2.16.2")
			github("SkriptLang", "skript-reflect", "v2.6.3", "skript-reflect-2.6.3.jar")
			github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
			github("Fusezion", "IncomeEconomy", "1.1.2", "IncomeEconomy-1.1.2.jar")
		}
		minecraftVersion("1.21.11")
		jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
	}

	processResources {
		val props = mapOf("version" to version)
		filesMatching("paper-plugin.yml") {
			expand(props)
		}
	}
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
		}
	}
}