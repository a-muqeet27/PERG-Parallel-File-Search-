PERG

"Parallel File Search; A parallelized version of the grep tool that searches for a string pattern across multiple
files or within a large file"
	

Pre-requisites:-
->For C++ Backend:
 GCC compiler with OpenMP support
 C++11 or later
->For JavaFX GUI:
 Java 11 or later
 JavaFX SDK
 IDE with JavaFX support (VS Code, IntelliJ IDEA etc.)

Installation & Setup:-
1. Download and Setup OPENMP enivronment on your system. Use https://youtu.be/bkfCrj-rBjU?feature=shared

2. Download and Setup JAVAFX enivronment on your system. Use https://youtu.be/AubJaosfI-0?feature=shared

3. Create both files in same folder i.e. PERG.java and PERG.cpp

4. Download and Setup Gradle enivronment on your system.
  ->Go to https://gradle.org/releases/
  ->Download binary file under v8.14.2
  ->Set Environment Variables
    Go to Environment Variables → New System Variable
    Name: GRADLE_HOME
    Value: C:\Gradle\gradle-8.5
    Add to PATH
    Edit system variable Path
    Add: C:\Gradle\gradle-8.5\bin

5. Create file names build.gradle and copy following:

plugins {
    id 'application'
}

repositories {
    mavenCentral()
}



dependencies {
    def javafxVersion = '21'

    implementation "org.openjfx:javafx-controls:$javafxVersion"
    implementation "org.openjfx:javafx-fxml:$javafxVersion"
    implementation "org.openjfx:javafx-graphics:$javafxVersion"

    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
}

application {
    mainClass = 'org.example.PERG'

    applicationDefaultJvmArgs = [
        '--enable-native-access=ALL-UNNAMED',
        '--add-opens=java.base/java.lang=ALL-UNNAMED',
        '--add-opens=java.base/sun.misc=ALL-UNNAMED'
    ]
}

test {
    useJUnitPlatform()
}

def javafxLibPath = 'C:/Users/dell/Downloads/javafx-sdk-24.0.1/lib'

run {
    jvmArgs = [
        '--module-path', javafxLibPath,
        '--add-modules', 'javafx.controls,javafx.fxml'
    ]
}

compileJava {
    options.compilerArgs += [
        '--module-path', javafxLibPath,
        '--add-modules', 'javafx.controls,javafx.fxml'
    ]
}

compileTestJava {
    options.compilerArgs += [
        '--module-path', javafxLibPath,
        '--add-modules', 'javafx.controls,javafx.fxml'
    ]
}



6. Compile the C++ Backend
 (Navigate to your project directory e.g. cd /path/to/your/project)
  g++ perg.cpp -o perg.exe -fopenmp
 

7. Compile the JAVAFX Frontend
  (Navigate to your project directory i.e. main folder)
  gradle build (then press enter and build starts)
  gradle run
  
And finally the PERG starts performing
