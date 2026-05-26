plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.serialization") version "2.3.21"
	id("com.gradleup.shadow") version "9.4.1"
	id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.nightexpressdev.com/releases")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
	compileOnly("su.nightexpress.excellenteconomy:ExcellentEconomy:2.8.0")
	compileOnly("su.nightexpress.nightcore:main:2.15.3")
	implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
	implementation("dev.jorel:commandapi-paper-shade:11.2.0")
	implementation("dev.jorel:commandapi-kotlin-paper:11.2.0")
}

kotlin {
	jvmToolchain(25)
}

tasks {

	shadowJar {
		archiveClassifier = ""
		relocate("dev.jorel.commandapi", "dev.lyric.income.sell.libs.commandapi")
	}

	build {
		dependsOn(shadowJar)
	}

	runServer {
		// Configure the Minecraft version for our task.
		// This is the only required configuration besides applying the plugin.
		// Your plugin's jar (or shadowJar if present) will be used automatically.
		downloadPlugins {
			modrinth("skript", "2.15.2")
			modrinth("excellenteconomy", "2.8.0")
			github("SkriptLang", "skript-reflect", "v2.6.3", "skript-reflect-2.6.3.jar")
			github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
			modrinth("nightcore", "2.15.2")
		}
		minecraftVersion("1.21.11")
		jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
	}

	processResources {
		val props = mapOf("version" to version, "description" to project.description)
		filesMatching("paper-plugin.yml") {
			expand(props)
		}
	}
}
