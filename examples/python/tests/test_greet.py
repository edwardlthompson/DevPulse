"""Tests for hello.greet module."""

import pytest

from hello.greet import greet, validate_name


class TestGreet:
    def test_default_greeting(self) -> None:
        assert greet("") == "Hello, world!"
        assert greet("   ") == "Hello, world!"

    def test_personalized_greeting(self) -> None:
        assert greet("FOSS") == "Hello, FOSS!"


class TestValidateName:
    def test_strips_whitespace(self) -> None:
        assert validate_name("  FOSS  ") == "FOSS"

    def test_rejects_long_names(self) -> None:
        with pytest.raises(ValueError, match="100 characters"):
            validate_name("x" * 101)
