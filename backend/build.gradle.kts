plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.0.2"
}

group = "coffeeshout"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation(platform("io.micrometer:micrometer-bom:1.15.2"))
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.kjetland:mbknor-jackson-jsonschema_2.13:1.0.39")
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.0")
    implementation("com.github.victools:jsonschema-generator:4.37.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

node {
    version.set("20.16.0")
    download.set(true)
    npmInstallCommand.set("install")
}

/**
 * 1. JavaExec 태스크로 asyncapi.yml 생성
 */
tasks.register<JavaExec>("generateAsyncApiYaml") {
    group = "documentation"
    description = "Generate asyncapi.yml using AsyncApiGenerator"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("coffeeshout.generator.AsyncApiGenerator")
    outputs.file("asyncapi.yml")
}

/**
 * 2. AsyncAPI CLI로 HTML 생성
 */
val generateAsyncApiHtml = tasks.register<Exec>("generateAsyncApiHtml") {
    group = "documentation"
    description = "Generate HTML docs from asyncapi.yml"

    dependsOn("generateAsyncApiYaml")

    val outputDir = layout.buildDirectory.dir("generated-docs/static/docs")

    commandLine(
        "npx", "-y", "@asyncapi/cli@latest",
        "generate", "fromTemplate", "asyncapi.yml",
        "@asyncapi/html-template@3.3.1",
        "--use-new-generator",
        "-o", outputDir.get().asFile.absolutePath,
        "--force-write",
        "-p", "singleFile=true"
    )
    outputs.dir(outputDir)
}

/**
 * 3. bootJar에 결과물 포함시키기
 */
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    dependsOn(generateAsyncApiHtml) // jar 만들기 전에 docs 생성
    from(layout.buildDirectory.dir("generated-docs")) {
        into("BOOT-INF/classes/") // 리소스로 들어가게
    }
}
