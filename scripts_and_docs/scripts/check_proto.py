#!/usr/bin/env python3
"""
Check that proto files are properly generated and can be used.
"""
from client.enrollmentProto_pb2 import CreateUserRequest, CreateUserResponse
from client.common_pb2 import SignedResponse
from client.errorResponse_pb2 import ErrorResponse, ErrorCode
from client.user_pb2 import User
from client.login_pb2 import Login, LoginResponse

def test_enrollment_proto():
    """Test enrollmentProto messages"""
    print("Testing enrollmentProto...")

    # Test CreateUserRequest
    req = CreateUserRequest()
    req.name = "Test User"
    req.email = "test@example.com"
    serialized = req.SerializeToString()
    assert len(serialized) > 0, "CreateUserRequest serialization failed"

    req2 = CreateUserRequest()
    req2.ParseFromString(serialized)
    assert req2.name == "Test User", "CreateUserRequest deserialization failed"
    assert req2.email == "test@example.com", "CreateUserRequest deserialization failed"
    print("  ✓ CreateUserRequest")

    # Test CreateUserResponse
    resp = CreateUserResponse()
    resp.registrationCode = "ABC123"
    resp.userId = "user-123"
    resp.message = "Success"
    serialized = resp.SerializeToString()
    assert len(serialized) > 0, "CreateUserResponse serialization failed"
    print("  ✓ CreateUserResponse")

def test_common_proto():
    """Test common proto messages"""
    print("Testing common proto...")

    resp = SignedResponse()
    resp.payload = b"test payload"
    resp.signature = b"test signature"
    serialized = resp.SerializeToString()
    assert len(serialized) > 0, "SignedResponse serialization failed"
    print("  ✓ SignedResponse")

def test_error_proto():
    """Test error response proto"""
    print("Testing errorResponse proto...")

    err = ErrorResponse()
    err.error_id = "err-123"
    err.error_code = ErrorCode.VALIDATION_ERROR
    err.message = "Validation failed"
    serialized = err.SerializeToString()
    assert len(serialized) > 0, "ErrorResponse serialization failed"
    print("  ✓ ErrorResponse")
    print("  ✓ ErrorCode enum")

def test_user_proto():
    """Test user proto"""
    print("Testing user proto...")

    user = User()
    user.id = 1
    user.name = "Test User"
    user.email = "test@example.com"
    serialized = user.SerializeToString()
    assert len(serialized) > 0, "User serialization failed"
    print("  ✓ User")

def test_login_proto():
    """Test login proto"""
    print("Testing login proto...")

    login = Login()
    login.email = "test@example.com"
    login.token = "token-123"
    serialized = login.SerializeToString()
    assert len(serialized) > 0, "Login serialization failed"

    login_resp = LoginResponse()
    login_resp.success = True
    login_resp.message = "Login successful"
    serialized = login_resp.SerializeToString()
    assert len(serialized) > 0, "LoginResponse serialization failed"
    print("  ✓ Login")
    print("  ✓ LoginResponse")

if __name__ == "__main__":
    try:
        test_enrollment_proto()
        test_common_proto()
        test_error_proto()
        test_user_proto()
        test_login_proto()
        print("\n✓ All proto files are properly generated and usable!")
    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
