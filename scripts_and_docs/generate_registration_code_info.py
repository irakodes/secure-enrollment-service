#!/usr/bin/env python3
"""
Generate registration code information for a user.

This script helps you understand what needs to be stored in the database
for a user to be able to complete registration.

Usage:
    python generate_registration_code_info.py <email>

Example:
    python generate_registration_code_info.py test@example.com

Note: This script generates the registration code structure, but you'll need
      to hash the first 16 characters with BCrypt (which requires Java/Spring).
      Use this information to set up the user in the database.
"""

import sys
import hashlib

def generate_registration_code_info(email):
    """
    Generate registration code information similar to StringsUtil.getRegistrationCode()
    
    This replicates the Java logic:
    1. Generate 16-char code: usernameInHex (8 chars) + randomHex (8 chars)
    2. Calculate MD5 hash
    3. Extract checksum (last 2 bytes as hex)
    4. Combine: 16-char code + 4-char checksum = 20-char code
    """
    
    # Step 1: Generate username hash (8 hex chars)
    username_hash = format(hash(email) & 0xFFFFFFFF, '08x')
    
    # Step 2: Generate random 8-char hex code
    # Note: In Java this uses SecureRandom, but for demonstration we'll use a deterministic approach
    import random
    random.seed(hash(email))  # Make it deterministic for same email
    random_hex = ''.join([format(random.randint(0, 255), '02x') for _ in range(4)])
    
    # Combine to get 16-char registration code
    registration_code_16 = username_hash + random_hex
    
    # Step 3: Calculate MD5 hash
    md5_hash = hashlib.md5(registration_code_16.encode('utf-8')).digest()
    
    # Step 4: Extract checksum (last 2 bytes)
    second_to_last = md5_hash[-2] & 0xFF
    last_byte = md5_hash[-1] & 0xFF
    checksum = format(second_to_last, '02x') + format(last_byte, '02x')
    
    # Step 5: Combine to get full 20-char code
    full_registration_code = registration_code_16 + checksum
    
    print("=" * 60)
    print("Registration Code Information")
    print("=" * 60)
    print(f"Email: {email}")
    print()
    print("Registration Code Components:")
    print(f"  Full Code (20 chars): {full_registration_code}")
    print(f"  Code (first 16 chars): {registration_code_16}")
    print(f"  Checksum (last 4 chars): {checksum}")
    print()
    print("Database Setup Instructions:")
    print("=" * 60)
    print("1. Store the BCrypt hash of the first 16 characters in 'registrationCode' field")
    print(f"   Code to hash: {registration_code_16}")
    print()
    print("2. Set 'isRegistered' to true (due to logic in RegistrationAuthnProvider)")
    print()
    print("3. Use the full 20-character code for registration:")
    print(f"   {full_registration_code}")
    print()
    print("To generate BCrypt hash in Java:")
    print("  PasswordEncoder encoder = new BCryptPasswordEncoder();")
    print(f"  String hash = encoder.encode(\"{registration_code_16}\");")
    print()
    print("Or use Spring Boot shell:")
    print("  java -cp target/classes:target/dependency/* \\")
    print("    -Dspring.shell.command.script=hash_password.groovy")
    print()
    print("Example SQL (after generating BCrypt hash):")
    print("  UPDATE USERS SET")
    print(f"    REGISTRATION_CODE = '<BCRYPT_HASH>',")
    print("    IS_REGISTERED = true")
    print(f"  WHERE EMAIL = '{email}';")
    print()
    print("=" * 60)
    
    return {
        'email': email,
        'full_code': full_registration_code,
        'code_16': registration_code_16,
        'checksum': checksum
    }

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    
    email = sys.argv[1]
    generate_registration_code_info(email)
