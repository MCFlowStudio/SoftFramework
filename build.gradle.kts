import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
    kotlin("jvm") version "1.7.21"
}

group = "com.softhub"
version = "1.0.3-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.minebench.de/")
}

configurations.all {
    resolutionStrategy {
        force("org.yaml:snakeyaml:1.33")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    implementation("org.yaml:snakeyaml:1.33")

    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
    implementation("org.xerial:sqlite-jdbc:3.34.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("org.jetbrains.kotlin:kotlin-test:1.7.21")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.21")
}

bukkitPluginYaml {
    main = "com.softhub.softframework.BukkitFrameworkPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("minhyeok")
    apiVersion = "1.17"
    libraries.add("org.jetbrains.kotlin:kotlin-stdlib:1.7.21")
    libraries.add("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
    libraries.add("org.jetbrains.kotlin:kotlin-test:1.7.21")
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.softhub"
            artifactId = "soft-framework"
            version = "1.0.3-SNAPSHOT"

            from(components["java"])
        }
    }
}
tasks.processResources {
    val props = mapOf(
        "version" to version,
        "project" to mapOf("name" to project.name, "version" to project.version)
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.jar {
    enabled = true
    archiveBaseName.set(project.rootProject.name)
    archiveVersion.set("")
    archiveClassifier.set("")
    finalizedBy(tasks.shadowJar)
    destinationDirectory.set(file(rootProject.projectDir.path + "/build_outputs"))
}

sourceSets {
    named("main") {
        java.srcDir("src/main/java")
        resources.srcDir("src/main/resources")
    }
}