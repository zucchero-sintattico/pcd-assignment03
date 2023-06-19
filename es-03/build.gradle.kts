plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// Set Java 11
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}




// Run task, forward argument passed to gradle to GUI
tasks.register<JavaExec>("run") {
    // run rmiregistry before running the GUI
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("assignment.MainGUI")
}

for (i in 2..10) {
    tasks.register<JavaExec>("run$i") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("assignment.MainGUI")
        args(i.toString())
    }
}