# Secure Enrollment Service

A secure enrollment service built with Spring Boot, capable of enrolling entities with signed responses using Post-Quantum Cryptography (Dilithium).

## Prerequisites

- **Java 25**: This project explicitly requires Java 25.
- **Maven**: For building and dependency management.

## Getting Started

1.  **Clone the repository** to your local machine.
2.  **Build the project** and generate Protobuf sources:
    ```bash
    ./mvnw clean install
    ```
3.  **Run the application**:
    ```bash
    ./mvnw spring-boot:run
    ```

The application will start on port **8443**.

## How to Use

### gRPC API
The service uses gRPC for communication. Key messages and flows include:
- **User Creation**: `CreateUserRequest` -> `CreateUserResponse` (returns a unique `registrationCode`).
- **User Registration**: `RegisterUserRequest` (requires `registrationCode`) -> `RegisterUserResponse` (returns `authToken`).
- **Admin Login**: `AdminLoginRequest` -> `AdminLoginResponse`.
- **Signed Responses**: Responses are wrapped in `SignedResponse` containing the `body` and a Dilithium `signature`.

Start by reviewing `src/main/proto/enrollmentProto.proto` for the complete service definition.

### Scripts
Helper scripts are located in the `scripts/` directory to facilitate testing and interaction with the service:
- `create_and_send_registration.sh`
- `create_and_send_user_request.sh`

### Configuration
- **Database**: H2 (File-based) located at `./data/ses_db`.
- **H2 Console**: Enabled. Accessible at `http://localhost:8443/h2-console`.
    - JDBC URL: `jdbc:h2:file:./data/ses_db`
    - Username: `sa`
    - Password: `runH2N0w`
- **Keys**: Cryptographic keys are stored in `./data/keys`.

## System Flow

The following diagram illustrates the secure enrollment and response signing process:

```text
+-------+             +-----------------+             +-----------+
| ADMIN |             |     SERVICE     |             | DATABASE  |
+-------+             +-----------------+             +-----------+
    |                          |                            |
    | (1) CreateUserRequest    |                            |
    |------------------------->|                            |
    |                          | (2) Save Pending User      |
    |                          |--------------------------->|
    |                          |                            |
    | (3) CreateUserResponse   |                            |
    |     (RegistrationCode)   |                            |
    |<-------------------------|                            |
    |                          |                            |
                               |                            |
+------+                       |                            |             +-----------+
| USER |                       |                            |             | KEY STORE |
+------+                       |                            |             |           |
    |                          |                            |             +-----------+
    | (4) RegisterUserRequest  |                            |                   |
    |     (Code + Email)       |                            |                   |
    |------------------------->|                            |                   |
    |                          | (5) Validate Code          |                   |
    |                          |--------------------------->|                   |
    |                          |                            |                   |
    |                          | (6) Sign Response          |                   |
    |                          |     (Dilithium Pvt Key)    |                   |
    |                          |------------------------------------------->|   |
    |                          |                            |               |---|
    |                          |                            |<--------------|   |
    | (7) SignedResponse       |                            |                   |
    |     (Token + Signature)  |                            |                   |
    |<-------------------------|                            |                   |
    |                          |                            |                   |
    | (8) GetPublicKey         |                            |                   |
    |------------------------->|                            |                   |
    |                          |                            |                   |
    | (9) PublicKeyResponse    |                            |                   |
    |<-------------------------|                            |                   |
    |                          |                            |                   |
   (10) Verify Signature       |                            |                   |
```
