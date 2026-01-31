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
│   │   ├── kotlin/com/example/frlineagent/
│   │   │   ├── presentation/   # Handles external HTTP requests and validation (Routing)
│   │   │   ├── domain/         # Business logic and abstractions (Service, Interfaces, Models)
│   │   │   ├── infrastructure/ # Concrete implementations of external APIs/SDKs (Google, LINE, GCP)
│   │   │   ├── plugins/        # Ktor framework configurations (DI, Serialization, Error Handling)
│   │   │   └── Application.kt  # Application entry point
│   │   └── resources/          # Configuration files and logging settings
│   └── test/                   # Unit and integration tests
├── gradle/                     # Gradle wrapper and version catalogs
├── docs/                       # UML diagrams and documentations
├── build.gradle.kts            # Build configuration
└── settings.gradle.kts         # Project settings
```

### Layer Responsibilities

| Package            | Description                                                                                                                                                                                             |
|:-------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **presentation**   | Defines HTTP endpoints and handles request/response cycles. It manages input validation (e.g., header checks) and delegates business logic to the domain layer.                                         |
| **domain**         | Represents the core of the application. It contains business logic, data models, and interface definitions (abstractions) that describe "what" the system does regardless of the underlying technology. |
| **infrastructure** | Implements the interfaces defined in the domain layer. it handles technical details such as calling LINE Messaging API, Google Sheets API, or interacting with Google Cloud Secret Manager.             |
| **plugins**        | Manages Ktor-specific configurations and cross-cutting concerns, including Dependency Injection (Koin), Content Negotiation (Serialization), and global exception handling (StatusPages).               |

## License
This project is licensed under the Apache License, Version 2.0. See the [LICENSE.txt](LICENSE.txt) file for details.
