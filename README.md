# Secure Enrollment Service

A secure enrollment service built with Spring Boot, capable of enrolling entities with signed responses using Post-Quantum Cryptography (Dilithium).

## Prerequisites

- **Java 25**: This project explicitly requires Java 25.
- **Maven**: For building and dependency management.
- **Protocol Buffers**: Protobuf compiler (`protoc`) - automatically downloaded by Maven plugin, or install manually for command-line use.

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

### REST API (Protobuf Binary Format)

The service uses REST endpoints with Protocol Buffers in binary format (`application/octet-stream`). All responses are automatically signed with Dilithium post-quantum signatures.

#### Endpoints

- **POST `/api/users`** - Create a new user
  - Request: `CreateUserRequest` (name, email)
  - Response: `CreateUserResponse` (registrationCode, userId, message)
  - Returns: HTTP 201 with signed response

- **POST `/api/register/complete`** - Complete user registration
  - Request: `RegisterUserRequest` (email, registrationCode)
  - Response: `RegisterUserResponse` (authToken, username, email, message)
  - Returns: HTTP 200 with signed response

- **GET `/api/users/get`** - Get user information
  - Response: `User` (id, name, email)
  - Returns: HTTP 200 with signed response

- **POST `/api/users/login`** - User login
  - Request: `Login` (email, token)
  - Response: `LoginResponse` (success, message)
  - Returns: HTTP 200 with signed response

- **GET `/api/keys/public`** - Get Dilithium public key
  - Response: `PublicKeyResponse` (dilithiumPublicKey, keyAlgorithm, keyGeneratedAt)
  - Returns: HTTP 200

#### Response Format

All API responses (except errors) are automatically wrapped in `SignedResponse`:
- **body**: The serialized protobuf message
- **signature**: Dilithium digital signature of the body (NIST FIPS 204 compliant)

### Web UI for Testing

A web interface is available for testing the registration flow and viewing signed responses:

- **URL**: `http://localhost:8443/register`
- **Features**:
  - Create users and view registration codes
  - Complete user registration and view auth tokens
  - Parse and display signed protobuf responses
  - View Dilithium signatures in hex format

### Protocol Buffer Definitions

Start by reviewing `src/main/proto/enrollmentProto.proto` for the complete message definitions.

### Scripts
Helper scripts are located in the `scripts_and_docs/` directory to facilitate testing and interaction with the service:
- `create_user_complete.sh` - Complete user creation and registration flow
- `test-registration.sh` - Test registration endpoints
- `generate_request.py` - Generate protobuf request messages
- See `scripts_and_docs/` for additional testing utilities

### Configuration

#### Application Properties
- **Server Port**: 8443
- **Database**: H2 (File-based) located at `./data/ses_db`
- **H2 Console**: Enabled. Accessible at `http://localhost:8443/h2-console`
    - JDBC URL: `jdbc:h2:file:./data/ses_db`
    - Username: `sa`
    - Password: `runH2N0w`

#### Cryptographic Configuration
- **Key Storage**: Dilithium keys are stored in `./data/keys`
  - Private key: `dilithium_private.key` (PKCS#8 format)
  - Public key: `dilithium_public.key` (X.509 format)
- **Security Level**: Configurable via `dilithium.security.level` (default: 2)
  - Level 2: Dilithium2 (128-bit security) - Recommended for most use cases
  - Level 3: Dilithium3 (192-bit security)
  - Level 5: Dilithium5 (256-bit security)

#### Post-Quantum Cryptography
- **Algorithm**: CRYSTALS-Dilithium (ML-DSA per NIST FIPS 204)
- **Implementation**: BouncyCastle Post-Quantum Cryptography library
- **Compliance**: Follows NIST FIPS 204 standards for Module-Lattice-Based Digital Signature Algorithm
- **Key Generation**: Automatic on first startup, persisted to disk

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
