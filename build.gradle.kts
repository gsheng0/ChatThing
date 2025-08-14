plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("black.ninia:jep:4.2.0")
    implementation("com.google.genai:google-genai:1.1.0")
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
    implementation("com.openai:openai-java:3.0.1")
    implementation("org.im4java:im4java:1.4.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks.test {
    useJUnitPlatform()
}


tasks.withType<JavaExec> {
    jvmArgs("-Djava.library.path=/opt/homebrew/lib")
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

application {
    mainClass = "org.scheduler.Main"
}