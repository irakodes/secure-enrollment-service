#!/usr/bin/env python3
"""
Authenticate a user via the /api/users/login endpoint.

This script sends a Login protobuf message to authenticate a user.

Usage:
    python login_user.py <email> <token> [output_file]

Example:
    python login_user.py john@example.com "abc123def456" login_request.bin

The endpoint returns a LoginResponse with success status and message.
"""

import sys

def encode_varint(value):
    """Encode an integer as a protobuf varint."""
    result = []
    while value > 0x7F:
        result.append((value & 0x7F) | 0x80)
        value >>= 7
    result.append(value & 0x7F)
    return bytes(result)

def create_login_request(email, token, output_file):
    """
    Create a protobuf Login message with correct varint encoding.
    
    Message structure:
        message Login {
            string email = 1;
            string token = 2;
        }
    """
    
    # Encode email (field 1, wire type 2)
    email_bytes = email.encode('utf-8')
    email_tag = (1 << 3) | 2  # field 1, wire type 2
    
    # Encode token (field 2, wire type 2)
    token_bytes = token.encode('utf-8')
    token_tag = (2 << 3) | 2  # field 2, wire type 2
    
    # Write to file
    with open(output_file, 'wb') as f:
        # Write email field: tag + varint length + data
        f.write(bytes([email_tag]))
        f.write(encode_varint(len(email_bytes)))
        f.write(email_bytes)
        
        # Write token field: tag + varint length + data
        f.write(bytes([token_tag]))
        f.write(encode_varint(len(token_bytes)))
        f.write(token_bytes)
    
    print(f"✓ Created protobuf login request file: {output_file}")
    print(f"  Email: {email}")
    print(f"  Token: {token}")
    print()
    print("Use with curl:")
    print(f"  curl -k -X POST http://localhost:8443/api/users/login \\")
    print(f"    -H 'Content-Type: application/octet-stream' \\")
    print(f"    -H 'Accept: application/octet-stream' \\")
    print(f"    --data-binary @{output_file} \\")
    print(f"    -v -i")
    print()
    print("Or with HTTPie:")
    print(f"  http POST http://localhost:8443/api/users/login \\")
    print(f"    Content-Type:application/octet-stream \\")
    print(f"    Accept:application/octet-stream \\")
    print(f"    < {output_file} --verify=no -v")
    print()
    print("Expected response: 200 OK with LoginResponse")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    
    email = sys.argv[1]
    token = sys.argv[2]
    output_file = sys.argv[3] if len(sys.argv) > 3 else "login_request.bin"
    
    # Basic email validation
    if '@' not in email or '.' not in email.split('@')[1]:
        print(f"Warning: '{email}' doesn't look like a valid email address")
        response = input("Continue anyway? (y/n): ")
        if response.lower() != 'y':
            sys.exit(1)
    
    # Basic token validation
    if not token or len(token.strip()) == 0:
        print(f"Warning: Token appears to be empty")
        response = input("Continue anyway? (y/n): ")
        if response.lower() != 'y':
            sys.exit(1)
    
    create_login_request(email, token, output_file)