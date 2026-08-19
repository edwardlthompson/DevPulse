"""Property-based tests for hello.greet (Hypothesis)."""

from hypothesis import given
from hypothesis import strategies as st

from hello.greet import greet, validate_name


@given(st.text(max_size=100))
def test_greet_always_starts_with_hello(name: str) -> None:
    result = greet(name)
    assert result.startswith("Hello, ")
    assert result.endswith("!")


@given(st.text(min_size=1, max_size=100).filter(lambda s: bool(s.strip())))
def test_greet_includes_trimmed_name(name: str) -> None:
    trimmed = name.strip()
    assert greet(name) == f"Hello, {trimmed}!"


@given(st.text(max_size=100))
def test_validate_name_idempotent_strip(name: str) -> None:
    assert validate_name(name) == name.strip()
