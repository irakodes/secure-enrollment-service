#!/usr/bin/env python3

"""
user_login_flow.py

Step 1: Create User
Binary Protobuf over HTTP + SignedResponse Handling
"""

import logging
import sys
from typing import Tuple, Optional

import requests
from google.protobuf.message import DecodeError

# System Objects and Classes
# Generated protobuf imports
from client.enrollmentProto_pb2 import CreateUserRequest, CreateUserResponse
from client.common_pb2 import SignedResponse
from client.errorResponse_pb2 import ErrorResponse
from data_helper import create_data_helper_service

BASE_URL = "http://127.0.0.1:8443"
CREATE_USER_ENDPOINT = "api/users"

HEADERS = {
    "Content-Type": "application/octet-stream",
    "Accept": "application/octet-stream"
}

TIMEOUT_IN_SECONDS = 10


def setup_logging() -> None:
    logging.basicConfig(
        level=logging.DEBUG,
        format="[%(asctime)s] %(levelname)s:%(name)s:%(message)s",
        handlers=[
            logging.FileHandler("client.log"),
            logging.StreamHandler(sys.stdout),
        ],
    )


log = logging.getLogger("secure-enrollment-client")


# Helper Functions
def build_create_user_request(username: str, email: str) -> bytes:
    log.debug("Building CreateUserRequest protobuf")

    request = CreateUserRequest(name=username, email=email)

    data = request.SerializeToString()
    log.debug("Serialized CreateUserRequest (%d bytes)", len(data))
    return data


def send_request(binary_data: bytes) -> bytes:
    url = f"{BASE_URL}/{CREATE_USER_ENDPOINT}"
    log.info("POST %s", url)

    try:
        response = requests.post(url, headers=HEADERS, data=binary_data, timeout=TIMEOUT_IN_SECONDS)
        # response.raise_for_status()

    except requests.exceptions.RequestException as e:
        log.error("HTTP error sending request: %s", e, exc_info=True)
        raise RuntimeError("Failed to send request") from e

    log.info("HTTP %s", response.status_code)

    if response.status_code not in (200, 201):
        raise RuntimeError(f"Unexpected response status: {response.status_code}")

    if not response.content:
        raise RuntimeError("Empty response content")

    log.debug("Received response (%d bytes)", len(response.content))
    return response.content


def parse_signed_response(raw: bytes) -> Tuple[bytes, bytes]:
    log.debug("Parsing SignedResponse protobuf")

    signed = SignedResponse()

    try:
        signed.ParseFromString(raw)

    except DecodeError as e:
        log.error("Failed to parse SignedResponse protobuf: %s", e, exc_info=True)
        raise

    log.debug("Signed payload=%d bytes, signature=%d bytes,",
              len(signed.payload), len(signed.signature))

    return signed.payload, signed.signature

def parse_create_user_response(payload: bytes) -> CreateUserResponse:
    response = CreateUserResponse()

    try:
        response.ParseFromString(payload)
    except DecodeError as exc:
        log.error("CreateUserResponse parsing failed", exc_info=True)
        raise

    return response

def try_parse_success(payload: bytes) -> Optional[CreateUserResponse]:
    response = CreateUserResponse()
    try:
        response.ParseFromString(payload)
        return response
    except DecodeError:
        return None

def try_parse_error(payload: bytes) -> Optional[ErrorResponse]:
    error = ErrorResponse()
    try:
        error.parseFromString(payload)
        return error
    except DecodeError:
        return None

def main() -> None:
    setup_logging()
    log.info("[STEP 1] -- Creating A User")

    # Step 1: Input (To Configure Later)
    # --- Using A Random Test User ---
    data_service = create_data_helper_service()
    random_user = data_service.get_random_user()

    name = random_user['Names']
    email = random_user['Email']

    request_bytes = build_create_user_request(name, email)
    raw_response = send_request(request_bytes)

    payload, signature = parse_signed_response(raw_response)
    log.debug("Dilithium Signature Hex: %s", signature.hex())

    #create_user_response = parse_create_user_response(payload)
    # --- Attempting to parse the success response first ---
    success = try_parse_success(payload)
    if success:
        log.info("User Created Successfully")
        log.info("User ID: %s", success.userId)
        return

    error = try_parse_error(payload)
    if error:
        log.error("Service error occurred")
        log.error("Error ID   : %s", error.errorId)
        log.error("Error Code : %s", error.errorCode)
        log.error("Message    : %s", error.message)
        log.error("Path       : %s", error.path)
        return

    # ---- Unknown payload
    log.critical("Unknown payload type received")
    raise RuntimeError("Unrecognized payload in SignedResponse")

if __name__ == "__main__":
    main()