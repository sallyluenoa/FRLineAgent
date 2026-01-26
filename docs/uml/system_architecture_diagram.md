# System Architecture Diagram

The FRLineAgent project utilizes a serverless architecture centered on Google Cloud Run.

- Cloud Run (Ktor): The core backend engine executing business logic. It handles incoming webhooks and scales automatically.
- Google Sheets API: Serves as the primary data source. The Ktor application fetches required information dynamically using a Service Account.
- Secret Manager: Securely stores sensitive information such as the Channel Secret and Channel Access Token for the LINE Messaging API.
- Cloud Logging / Monitoring: Automatically collects application logs from stdout. It provides real-time error reporting and performance monitoring.

```plantuml
@startuml
' External Systems
node "LINE Platform" as line #LightBlue
database "Google Sheets" as sheets #honeydew

' Google Cloud Project
package "Google Cloud Project" {
    [Cloud Run (Ktor)] as ktor
    [Secret Manager] as secret
    
    package "Observability" {
        [Cloud Logging] as logging
        [Cloud Monitoring] as monitoring
    }
}

' Main Flow
line -down-> ktor : Webhook
ktor -up-> line : Reply Message

' Confidential Data
ktor -right-> sheets : Fetch Data
ktor -left-> secret : Fetch Keys

' Operation Flow
ktor -down-> logging : Logs
logging -right-> monitoring : Metrics / Alert

@enduml
```
