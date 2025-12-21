#!/usr/bin/env python3
"""
Generate a protobuf registration request file for testing.

This script requires the protobuf Python package:
    pip install protobuf

Usage:
    python generate_request.py <email> <registration_code> [output_file]

Example:
    python generate_request.py test@example.com abc123def4567890abcd request.bin

Note: The registration_code must be a 20-character hex string with checksum.
      You can generate one using the Java StringsUtil.getRegistrationCode() method.
"""

import sys
import struct

def create_protobuf_request(email, registration_code, output_file):
    """
    Create a protobuf RegisterUserRequest message.

    Protobuf wire format for RegisterUserRequest:
    - Field 1 (email): string
    - Field 2 (registration_code): string

    Wire format: [field_number << 3 | wire_type] [length] [value]
    Wire type 2 = length-delimited (for strings)
    """

    # Encode email (field 1, wire type 2)
    email_bytes = email.encode('utf-8')
    email_tag = (1 << 3) | 2  # field 1, wire type 2
    email_length = len(email_bytes)

    # Encode registration_code (field 2, wire type 2)
    code_bytes = registration_code.encode('utf-8')
    code_tag = (2 << 3) | 2  # field 2, wire type 2
    code_length = len(code_bytes)

    # Write to file
    with open(output_file, 'wb') as f:
        # Write email field
        f.write(bytes([email_tag]))
        f.write(bytes([email_length]))
        f.write(email_bytes)

        # Write registration_code field
        f.write(bytes([code_tag]))
        f.write(bytes([code_length]))
        f.write(code_bytes)

    print(f"✓ Created protobuf request file: {output_file}")
    print(f"  Email: {email}")
    print(f"  Registration Code: {registration_code}")
    print()
    print("Use with curl:")
    print(f"  curl -k -X POST https://localhost:8443/api/register/complete \\")
    print(f"    -H 'Content-Type: application/octet-stream' \\")
    print(f"    -H 'Accept: application/octet-stream' \\")
    print(f"    --data-binary @{output_file}")
    print()
    print("Or with HTTPie:")
    print(f"  http POST https://localhost:8443/api/register/complete \\")
    print(f"    Content-Type:application/octet-stream \\")
    print(f"    Accept:application/octet-stream \\")
    print(f"    < {output_file} --verify=no")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)

    email = sys.argv[1]
    registration_code = sys.argv[2]
    output_file = sys.argv[3] if len(sys.argv) > 3 else "request.bin"

    if len(registration_code) != 20:
        print(f"Error: Registration code must be exactly 20 characters")
        print(f"       Got: {len(registration_code)} characters")
        sys.exit(1)

    create_protobuf_request(email, registration_code, output_file)
