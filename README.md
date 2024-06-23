# Lint File

[![License](https://img.shields.io/github/license/lumkit/LintFile)](LICENSE)
[![Version](https://img.shields.io/github/v/release/lumkit/LintFile?include_prereleases)](https://github.com/lumkit/LintFile/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lumkit/LintFile)](https://central.sonatype.com/artifact/io.github.lumkit/LintFile/)
A file operation library suitable for Android platform.

# Introduce

This is a multifunctional library that simplifies the steps for developers to access the disk files
of Android devices.
It supports the basic Java File Api, integrates advanced file operation Apis such as "Su File" and "
Shizuku File",
and supports automatic application for access rights.
Developers no longer need to care about adapting to new changes in different Android versions, and
everything is left to me!

# Features

I will compile it when I am free next time.😁

# Use this library in your project

1. Configure the Maven central warehouse for the project.

```kotlin
repositories {
    google()
    mavenCentral()
}
```

2. Import the [] dependency.

```kotlin
dependencies {
    implementation("io.github.lumkit:lint-file:1.0.0")
}
```

3. Initialize the library.

```kotlin
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Android context
        LintFileConfiguration.instance.init(this)

        // Perform other operations...
    }

    override fun onDestroy() {
        // Release the library.
        LintFileConfiguration.instance.destroy()
        super.onDestroy()
    }
}
```