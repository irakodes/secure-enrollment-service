from client.enrollmentProto_pb2 import CreateUserRequest

req = CreateUserRequest(name = "Test User", email = "test@mail.com")
print(req.SerializeToString())