# Sequence Diagram: LINE Webhook Processing

This diagram illustrates the flow from receiving a LINE message to sending a reply, including signature verification and credential retrieval from GCP Secret Manager.

```plantuml
@startuml
skinparam style strictuml
skinparam MaxMessageSize 200

actor User
participant "LINE Platform" as LINE
box "FRLineAgent (Cloud Run)" #f9f9f9
    participant Routing
    participant "Secret Manager" as SM
    participant "Signature Verifier" as Verifier
    participant "Event Handler" as Handler
    participant "Business Logic" as Service
    participant "Line Messaging Client" as Client
end box

== Initialization / Fetch Credentials ==
note over Routing, SM: Credentials should be cached after first fetch
Routing -> SM: Access Channel Secret & Token
SM --> Routing: Secret / Token Strings

== Webhook Execution ==
User -> LINE: Sends message
LINE -> Routing: POST /webhook\nHeader: [X-Line-Signature]

Routing -> Verifier: Verify Signature\n(Payload, Signature, Secret)

alt Signature Valid
    Verifier --> Routing: Success
    Routing -> Handler: Dispatch Message Event
    
    Handler -> Service: Process Message
    Service --> Handler: Prepared Reply Text
    
    Handler -> Client: Call Reply API
    Client -> LINE: POST /v2/bot/message/reply
    LINE --> User: Deliver Reply Message
    
    Routing --> LINE: 200 OK
else Signature Invalid
    Verifier --> Routing: Failure
    Routing --> LINE: 401 Unauthorized
end

@enduml
```