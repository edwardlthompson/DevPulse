"""OpenAPI document stays; About payload import is removed with the slice."""

import json

from hello.log import ready_json
from hello.openapi import load_openapi, spec_paths


def test_openapi_lists_cli_paths() -> None:
    spec = load_openapi()
    assert spec["openapi"].startswith("3.")
    assert spec_paths() == ["/health", "/about", "/feedback"]


def test_ready_json_ok() -> None:
    assert json.loads(ready_json()) == {"status": "ok"}
