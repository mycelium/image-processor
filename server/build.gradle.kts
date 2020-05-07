plugins {
    java
    application
    eclipse
}

repositories {
    jcenter()
}


val logbackVersion="1.2.3"
dependencies {
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation("ch.qos.logback:logback-core:${logbackVersion}")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.xerial:sqlite-jdbc:3.30.1")
    implementation("com.google.code.gson:gson:2.8.6")
}

application {
    // Define the main class for the application.
    mainClassName = "ru.spbstu.amcp.impr.server.Main"
}
