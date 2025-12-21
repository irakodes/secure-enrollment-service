# Protocol Buffers JavaScript (protobuf.js) Reference Guide

A comprehensive guide for working with Protocol Buffers in JavaScript/TypeScript using protobuf.js, specifically for the Secure Enrollment Service.

## Table of Contents

1. [Introduction](#introduction)
2. [Installation](#installation)
3. [Basic Usage](#basic-usage)
4. [Working with Signed Responses](#working-with-signed-responses)
5. [Manual Parsing (Alternative Approach)](#manual-parsing-alternative-approach)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

## Introduction

Protocol Buffers (protobuf) is a language-neutral, platform-neutral extensible mechanism for serializing structured data. In this service, all API responses are serialized as binary protobuf messages and automatically signed with Dilithium post-quantum signatures.

### Why protobuf.js?

- **Browser Support**: Works in modern browsers without build tools
- **Type Safety**: Can generate TypeScript definitions
- **Performance**: Efficient binary serialization
- **Compatibility**: Works with protobuf messages from any language

## Installation

### CDN (Browser)

```html
<script src="https://cdn.jsdelivr.net/npm/protobufjs@7.4.0/dist/protobuf.min.js"></script>
```

### npm

```bash
npm install protobufjs
```

### Import

```javascript
// ES6 modules
import protobuf from 'protobufjs';

// CommonJS
const protobuf = require('protobufjs');
```

## Basic Usage

### Loading .proto Files

```javascript
// Load from URL
protobuf.load("path/to/enrollmentProto.proto", function(err, root) {
    if (err) throw err;
    
    // Get message types
    const CreateUserResponse = root.lookupType("enrollment.CreateUserResponse");
    const SignedResponse = root.lookupType("enrollment.SignedResponse");
});

// Or load from string
const protoDefinition = `
    syntax = "proto3";
    package enrollment;
    message CreateUserResponse {
        string registrationCode = 1;
        string userId = 2;
        string message = 3;
    }
`;

const root = protobuf.parse(protoDefinition).root;
const CreateUserResponse = root.lookupType("enrollment.CreateUserResponse");
```

### Encoding Messages (Sending Requests)

```javascript
// Create a message instance
const request = CreateUserRequest.create({
    name: "John Doe",
    email: "john@example.com"
});

// Encode to binary
const buffer = CreateUserRequest.encode(request).finish();

// Send via fetch
fetch('/api/users', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/octet-stream',
        'Accept': 'application/octet-stream'
    },
    body: buffer
});
```

### Decoding Messages (Receiving Responses)

```javascript
// Receive binary response
const response = await fetch('/api/users', options);
const arrayBuffer = await response.arrayBuffer();

// Decode the message
const message = CreateUserResponse.decode(new Uint8Array(arrayBuffer));

// Access fields
console.log(message.registrationCode);
console.log(message.userId);
console.log(message.message);
```

## Working with Signed Responses

In this service, all API responses are wrapped in a `SignedResponse` message containing:
- `body` (bytes): The serialized protobuf message
- `signature` (bytes): Dilithium digital signature

### Parsing Signed Responses

```javascript
// Load the SignedResponse type
const SignedResponse = root.lookupType("enrollment.SignedResponse");

// Receive binary response
const arrayBuffer = await response.arrayBuffer();
const signedResponse = SignedResponse.decode(new Uint8Array(arrayBuffer));

// Extract body and signature
const bodyBytes = signedResponse.body;
const signatureBytes = signedResponse.signature;

// Now decode the actual response message
const actualResponse = CreateUserResponse.decode(bodyBytes);
console.log("Registration Code:", actualResponse.registrationCode);
console.log("Signature (hex):", bytesToHex(signatureBytes));
```

### Complete Example: Create User Flow

```javascript
async function createUser(name, email) {
    // 1. Load proto definitions
    const root = await protobuf.load("/proto/enrollmentProto.proto");
    const CreateUserRequest = root.lookupType("enrollment.CreateUserRequest");
    const SignedResponse = root.lookupType("enrollment.SignedResponse");
    const CreateUserResponse = root.lookupType("enrollment.CreateUserResponse");
    
    // 2. Create request
    const request = CreateUserRequest.create({ name, email });
    const requestBuffer = CreateUserRequest.encode(request).finish();
    
    // 3. Send request
    const response = await fetch('/api/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/octet-stream',
            'Accept': 'application/octet-stream'
        },
        body: requestBuffer
    });
    
    // 4. Parse signed response
    const arrayBuffer = await response.arrayBuffer();
    const signedResponse = SignedResponse.decode(new Uint8Array(arrayBuffer));
    
    // 5. Extract and decode body
    const bodyMessage = CreateUserResponse.decode(signedResponse.body);
    
    // 6. Verify signature (if you have the public key)
    // const isValid = verifySignature(signedResponse.body, signedResponse.signature);
    
    return {
        registrationCode: bodyMessage.registrationCode,
        userId: bodyMessage.userId,
        message: bodyMessage.message,
        signature: signedResponse.signature
    };
}
```

## Manual Parsing (Alternative Approach)

If you cannot load .proto files (e.g., in a restricted environment), you can manually parse protobuf wire format:

### Protobuf Wire Format Basics

- **Tag**: Field number (3 bits) + Wire type (3 bits) encoded as varint
- **Wire Types**:
  - 0: Varint (int32, int64, uint32, uint64, sint32, sint64, bool, enum)
  - 1: 64-bit (fixed64, sfixed64, double)
  - 2: Length-delimited (string, bytes, embedded messages, packed repeated fields)
  - 5: 32-bit (fixed32, sfixed32, float)

### Reading Varints

```javascript
function readVarint(dataView, offset) {
    let value = 0;
    let shift = 0;
    let byte;
    
    do {
        byte = dataView.getUint8(offset++);
        value |= (byte & 0x7F) << shift;
        shift += 7;
    } while (byte & 0x80);
    
    return { value, offset };
}
```

### Parsing Length-Delimited Fields

```javascript
function parseLengthDelimited(dataView, offset) {
    // Read length
    const lengthInfo = readVarint(dataView, offset);
    offset = lengthInfo.offset;
    
    // Extract bytes
    const bytes = new Uint8Array(
        dataView.buffer,
        dataView.byteOffset + offset,
        lengthInfo.value
    );
    
    offset += lengthInfo.value;
    
    return { bytes, offset };
}
```

### Example: Manual SignedResponse Parser

```javascript
function parseSignedResponse(arrayBuffer) {
    const view = new DataView(arrayBuffer);
    let offset = 0;
    const result = { body: null, signature: null };
    
    while (offset < arrayBuffer.byteLength) {
        // Read tag
        const tagInfo = readVarint(view, offset);
        offset = tagInfo.offset;
        
        const fieldNumber = tagInfo.value >>> 3;
        const wireType = tagInfo.value & 0x7;
        
        if (wireType === 2) { // Length-delimited
            const fieldInfo = parseLengthDelimited(view, offset);
            offset = fieldInfo.offset;
            
            if (fieldNumber === 1) {
                result.body = fieldInfo.bytes;
            } else if (fieldNumber === 2) {
                result.signature = fieldInfo.bytes;
            }
        }
    }
    
    return result;
}
```

## Best Practices

### 1. Error Handling

```javascript
try {
    const message = MyMessage.decode(buffer);
    // Verify required fields
    const errMsg = MyMessage.verify(message);
    if (errMsg) throw Error(errMsg);
} catch (e) {
    if (e instanceof protobuf.util.ProtocolError) {
        console.error("Invalid protobuf format:", e);
    } else {
        console.error("Decode error:", e);
    }
}
```

### 2. Type Safety with TypeScript

```bash
# Generate TypeScript definitions
pbts -o enrollment.d.ts enrollmentProto.proto
```

```typescript
import { enrollment } from './enrollment';

const request: enrollment.ICreateUserRequest = {
    name: "John Doe",
    email: "john@example.com"
};
```

### 3. Reuse Root Object

```javascript
// Load once, reuse
let protoRoot = null;

async function getProtoRoot() {
    if (!protoRoot) {
        protoRoot = await protobuf.load("/proto/enrollmentProto.proto");
    }
    return protoRoot;
}
```

### 4. Handle Binary Data Correctly

```javascript
// ✅ Correct: Use Uint8Array
const buffer = new Uint8Array(arrayBuffer);
const message = MyMessage.decode(buffer);

// ❌ Wrong: Don't use ArrayBuffer directly
const message = MyMessage.decode(arrayBuffer); // Error!
```

### 5. Signature Verification

```javascript
// After parsing signed response
async function verifySignature(bodyBytes, signatureBytes, publicKey) {
    // Use Web Crypto API or a crypto library
    const cryptoKey = await crypto.subtle.importKey(
        "spki",
        publicKey,
        { name: "Dilithium" },
        false,
        ["verify"]
    );
    
    const isValid = await crypto.subtle.verify(
        "Dilithium",
        cryptoKey,
        signatureBytes,
        bodyBytes
    );
    
    return isValid;
}
```

## Troubleshooting

### Common Issues

#### 1. "Cannot find module" or "root is undefined"

**Problem**: Proto file not loaded correctly.

**Solution**:
```javascript
// Use async/await or proper callback
const root = await protobuf.load("path/to/proto.proto");
// Or
protobuf.load("path/to/proto.proto", (err, root) => {
    if (err) console.error(err);
    // Use root here
});
```

#### 2. "Invalid wire type" or parsing errors

**Problem**: Buffer offset issues or incorrect wire format.

**Solution**:
```javascript
// Ensure you're using the correct buffer
const buffer = new Uint8Array(arrayBuffer);
// Not: const buffer = arrayBuffer;

// Check buffer alignment
console.log("Buffer length:", buffer.length);
console.log("First bytes:", Array.from(buffer.slice(0, 10)));
```

#### 3. Signature verification fails

**Problem**: Body bytes don't match what was signed.

**Solution**:
- Ensure you're verifying the exact bytes from `signedResponse.body`
- Don't modify or re-encode the body before verification
- Check that the public key matches the private key used for signing

#### 4. Field values are undefined

**Problem**: Field names don't match proto definition (camelCase vs snake_case).

**Solution**:
```javascript
// Proto: registration_code
// Access: message.registrationCode (camelCase in JS)
// Or: message['registration_code'] (if using JSON name option)
```

### Debugging Tips

1. **Log raw bytes**:
```javascript
function bytesToHex(bytes) {
    return Array.from(bytes)
        .map(b => b.toString(16).padStart(2, '0'))
        .join('');
}

console.log("Response (hex):", bytesToHex(new Uint8Array(arrayBuffer)));
```

2. **Validate message structure**:
```javascript
const errMsg = MyMessage.verify(message);
if (errMsg) {
    console.error("Validation error:", errMsg);
}
```

3. **Check message type**:
```javascript
console.log("Message type:", message.$type.name);
console.log("Fields:", Object.keys(message));
```

## Additional Resources

- [protobuf.js Documentation](https://github.com/protobufjs/protobuf.js)
- [Protocol Buffers Language Guide](https://developers.google.com/protocol-buffers/docs/proto3)
- [Protocol Buffers Encoding](https://developers.google.com/protocol-buffers/docs/encoding)

## Project-Specific Notes

### Message Types in This Service

- `enrollment.CreateUserRequest` / `CreateUserResponse`
- `enrollment.RegisterUserRequest` / `RegisterUserResponse`
- `enrollment.SignedResponse` (wrapper for all responses)
- `enrollment.PublicKeyResponse`
- `login.Login` / `LoginResponse`
- `user.User`

### Response Flow

1. API endpoint returns a protobuf `Message`
2. `SigningResponseAdvice` intercepts and wraps in `SignedResponse`
3. Client receives binary `SignedResponse`
4. Client parses `SignedResponse` to extract `body` and `signature`
5. Client decodes `body` to get actual response message
6. Client can verify `signature` using public key from `/api/keys/public`

### Example: Complete Registration Flow

```javascript
// 1. Create user
const createResponse = await createUser("John", "john@example.com");
const registrationCode = createResponse.registrationCode;

// 2. Register user
const registerResponse = await registerUser("john@example.com", registrationCode);
const authToken = registerResponse.authToken;

// 3. Get public key for verification
const publicKeyResponse = await getPublicKey();
const publicKey = publicKeyResponse.dilithiumPublicKey;

// 4. Verify signature (optional)
const isValid = await verifySignature(
    registerResponse.bodyBytes,
    registerResponse.signature,
    publicKey
);
```

