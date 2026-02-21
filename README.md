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
To run the server locally with development-specific configurations (e.g., `application-local.yaml`), execute the following command. This command passes the necessary configuration files to the Ktor application.

```bash
./gradlew run --args='-config=src/main/resources/application.yaml -config=src/main/resources/application-local.yaml'
```

The server will start and listen on `http://localhost:8080` by default.

### Running with Docker (Local)
To verify the Docker image and run the application in a containerized environment locally, use Docker Compose. This setup mounts the local `src/main/resources` directory, allowing the container to use `application-local.yaml`.

```bash
# Build the image and start the container
docker compose up --build
```

To stop the container and remove the created resources:

```bash
docker compose down
```

### Testing Endpoints Locally

Once the server is running, you can use a tool like `curl` to test the API endpoints from another terminal window.

**Note for Windows Users:** If you are using PowerShell, the `curl` command is an alias for `Invoke-WebRequest`, which has a different syntax. To use the standard curl executable, you must specify `curl.exe`.

#### `/push` Endpoint

This endpoint triggers the scheduled push notification logic. It does not require a request body.

```bash
# Using -v for verbose output helps in debugging
# For Windows PowerShell, use curl.exe
curl -v -X POST http://localhost:8080/push
```

A successful request will return an HTTP `200 OK` status.

#### `/webhook` Endpoint

This endpoint simulates a webhook event from the LINE Platform. It requires a JSON body and a signature header. Sample request bodies are available in the `src/test/resources/request/webhook/` directory.

```bash
# Example using the sample for a user message
# For Windows PowerShell, use curl.exe
curl -v -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -H "X-Line-Signature: dummy_signature" \
  -d @src/test/resources/request/webhook/user_message.json
```

- **`X-Line-Signature`**: The signature verification is enabled on the server. Using a `dummy_signature` as shown will correctly result in an HTTP `401 Unauthorized` response, which confirms that the signature validation logic is working as expected.
- **`-d @...`**: This syntax loads the request body from the specified file. You can replace `user_message.json` with `group_mention.json` to test the group mention scenario.

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
│   │   ├── kotlin/org/fog_rock/frlineagent/
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
