#!/usr/bin/env python3

import logging

import requests
from pqcrypto.sign.dilithium2 import verify

from config import BASE_URL, TIMEOUT_IN_SECONDS
from scripts.config import HEADERS

log = logging.getLogger(__name__)


def fetch_dilithium_public_key() -> bytes:
    """
    Fetches the Dilithium2 public key from the server.
    """
    # TODO: Fetch from server
    url = f"{BASE_URL}/api/keys/public"
    log.info("Fetching Dilithium public key: %s", url)

    try:
        response = requests.get(
            url,
            headers=HEADERS,
            timeout=TIMEOUT_IN_SECONDS
        )
        response.raise_for_status()
    except requests.RequestException as e:
        log.exception("Failed to fetch Dilithium public key", exc_info=True)
        raise RuntimeError("Public Key endpoint unreachable") from e

    if response.status_code != 200:
        log.warning("The response code was not successful")
        raise RuntimeError(f"Failed to fetch public key (HTTP {response.status_code})")

    if not response.content:
        raise RuntimeError("Empty public key response received")

    log.info("Dilithium public key fetched (%d bytes)", len(response.content))
    #return bytes.fromhex(response.content.decode())
    return response.content