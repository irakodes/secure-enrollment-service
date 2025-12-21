#!/usr/bin/env python3
"""
Create a user via the /api/users/ endpoint.

This script sends a CreateUserRequest protobuf message to create a new user.

Usage:
    python create_user.py <name> <email> [output_file]

Example:
    python create_user.py "John Doe" john@example.com request.bin

The endpoint returns a 201 Created response with a Location header.
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

def create_protobuf_request_correct(name, email, output_file):
    """
    Create a protobuf CreateUserRequest message with correct varint encoding.
    """
    
    # Encode name (field 1, wire type 2)
    name_bytes = name.encode('utf-8')
    name_tag = (1 << 3) | 2  # field 1, wire type 2
    
    # Encode email (field 2, wire type 2)
    email_bytes = email.encode('utf-8')
    email_tag = (2 << 3) | 2  # field 2, wire type 2
    
    # Write to file
    with open(output_file, 'wb') as f:
        # Write name field: tag + varint length + data
        f.write(bytes([name_tag]))
        f.write(encode_varint(len(name_bytes)))
        f.write(name_bytes)
        
        # Write email field: tag + varint length + data
        f.write(bytes([email_tag]))
        f.write(encode_varint(len(email_bytes)))
        f.write(email_bytes)
    
    print(f"✓ Created protobuf request file: {output_file}")
    print(f"  Name: {name}")
    print(f"  Email: {email}")
    print()
    print("Use with curl:")
    print(f"  curl -k -X POST http://localhost:8443/api/users/ \\")
    print(f"    -H 'Content-Type: application/octet-stream' \\")
    print(f"    -H 'Accept: application/octet-stream' \\")
    print(f"    --data-binary @{output_file} \\")
    print(f"    -v -i")
    print()
    print("Or with HTTPie:")
    print(f"  http POST http://localhost:8443/api/users/ \\")
    print(f"    Content-Type:application/octet-stream \\")
    print(f"    Accept:application/octet-stream \\")
    print(f"    < {output_file} --verify=no -v")
    print()
    print("Expected response: 201 Created with Location header")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    
    name = sys.argv[1]
    email = sys.argv[2]
    output_file = sys.argv[3] if len(sys.argv) > 3 else "create_user_request.bin"
    
    # Basic email validation
    if '@' not in email or '.' not in email.split('@')[1]:
        print(f"Warning: '{email}' doesn't look like a valid email address")
        response = input("Continue anyway? (y/n): ")
        if response.lower() != 'y':
            sys.exit(1)
    
    create_protobuf_request_correct(name, email, output_file)
