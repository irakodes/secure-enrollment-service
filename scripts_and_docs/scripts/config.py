#!/usr/bin/env python3

"""
Configuration module for shared settings across the secure enrollment service scripts.

This module provides centralized configuration management following Python best practices:
- Environment variable support for easy configuration
- Sensible defaults
- Type hints for better IDE support
"""

import os
from typing import Dict


# Base URL configuration
# Can be overridden via BASE_URL environment variable
BASE_URL: str = os.getenv("BASE_URL", "http://127.0.0.1:8443")

# HTTP headers for protobuf requests
# These are consistent across all API calls
HEADERS: Dict[str, str] = {
    "Content-Type": "application/octet-stream",
    "Accept": "application/octet-stream"
}

# Request timeout in seconds
TIMEOUT_IN_SECONDS: int = int(os.getenv("REQUEST_TIMEOUT", "10"))
