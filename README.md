# FRLineAgent

## Overview
FRLineAgent is a backend application designed to facilitate the integration between the LINE Messaging API and Google Cloud Run.
It acts as an agent to handle webhook events from LINE and process them within a serverless environment on Google Cloud.

## Tools & Tech Stack
This project is built using the following technologies:

- **Language**: [Kotlin](https://kotlinlang.org/)
- **Framework**: [Ktor](https://ktor.io/) (Server-side framework)
- **Build System**: [Gradle](https://gradle.org/)
- **Serialization**: kotlinx.serialization
- **Logging**: Logback
- **Platform**: Google Cloud Run (Target deployment)

## How to Run

### Prerequisites
- JDK 17 or higher
- Gradle (optional, wrapper is included)

### Running Locally
To run the server locally, execute the following command:

```bash
./gradlew run
```

The server will start and listen on `http://localhost:8080` by default.

### Running Tests
To run the unit tests, execute:

```bash
./gradlew test
```

### Building
To build the project, run:

```bash
./gradlew build
```

To build a fat JAR (executable JAR with all dependencies):

```bash
./gradlew buildFatJar
```

## Project Structure

```
FRLineAgent/
├── src/
│   ├── main/
│   │   ├── kotlin/      # Kotlin source code
│   │   └── resources/   # Configuration files (application.yaml, etc.)
│   └── test/            # Unit and integration tests
├── gradle/              # Gradle wrapper and version catalogs
├── build.gradle.kts     # Build configuration
└── settings.gradle.kts  # Project settings
```

## License
This project is licensed under the Apache License, Version 2.0. See the [LICENSE.txt](LICENSE.txt) file for details.
