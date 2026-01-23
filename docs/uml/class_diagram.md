# Class Diagram: Full Layered Structure (Strict T-B Layout)

This diagram shows the complete class members and their strict hierarchical
relationships, ensuring that logic flow and dependency directions are clear.

```plantuml
@startuml
' --- Configuration ---
skinparam style strictuml
skinparam linetype ortho
skinparam packageStyle rectangle
top to bottom direction

' --- Presentation Layer ---
package "1. Presentation Layer" as Presentation #FEFEFE {
    class WebhookRoute {
        - service: LineBotService
        + handle(call: ApplicationCall): Unit
    }
    class PushTriggerRoute {
        - service: LineBotService
        + handle(call: ApplicationCall): Unit
    }
}

' --- Service Layer ---
package "2. Service Layer" as Service #F5F5F5 {
    class LineBotService {
        - sheetsRepo: SheetsRepository
        - lineClient: LineClient
        - verifier: SignatureVerifier
        + handleWebhook(body: String, signature: String): Result<Unit>
        + executeScheduledPush(): Result<Unit>
    }
    
    interface SignatureVerifier <<interface>> {
        + verify(body: String, signature: String): Boolean
    }
    
    interface SheetsRepository <<interface>> {
        + fetchNotificationData(): List<NotificationContent>
    }
    
    interface LineClient <<interface>> {
        + reply(token: String, message: String): Result<Unit>
        + push(userId: String, message: String): Result<Unit>
    }
    
    class NotificationContent <<data>> {
        + userId: String
        + message: String
    }
}

' --- Infrastructure Layer ---
package "3. Infrastructure Layer" as Infra #EEEEEE {
    class LineSignatureVerifierImpl {
        - channelSecret: String
        + verify(body: String, signature: String): Boolean
    }
    class GoogleSheetsRepositoryImpl {
        - spreadsheetId: String
        - credentialsJson: String
        + fetchNotificationData(): List<NotificationContent>
    }
    class LineMessagingClientImpl {
        - accessToken: String
        + reply(token: String, message: String): Result<Unit>
        + push(userId: String, message: String): Result<Unit>
    }
    class SecretManagerProvider {
        + getSecret(key: String): String
    }
}

' --- Layout Constraints (Hidden Links) ---
Presentation -[hidden]down-> Service
Service -[hidden]down-> Infra

' --- Real Relationships ---
' Presentation -> Service
WebhookRoute -down-> LineBotService
PushTriggerRoute -down-> LineBotService

' Service Logic Dependencies
LineBotService -right-> SignatureVerifier
LineBotService -right-> SheetsRepository
LineBotService -right-> LineClient
LineBotService .right.> NotificationContent

' Dependency Inversion: Infra implements Service Interfaces
LineSignatureVerifierImpl .up.|> SignatureVerifier
GoogleSheetsRepositoryImpl .up.|> SheetsRepository
LineMessagingClientImpl .up.|> LineClient

' Internal Infra usage
LineMessagingClientImpl .right.> SecretManagerProvider
LineSignatureVerifierImpl .right.> SecretManagerProvider

@enduml
```