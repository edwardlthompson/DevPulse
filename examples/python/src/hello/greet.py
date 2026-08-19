"""Typed greeting logic — pure business logic, decoupled from CLI."""


def greet(name: str) -> str:
    """Return a greeting for the given name."""
    trimmed = name.strip()
    if not trimmed:
        return "Hello, world!"
    return f"Hello, {trimmed}!"


def validate_name(name: str) -> str:
    """Validate and normalize a name input."""
    if len(name) > 100:
        raise ValueError("Name must be 100 characters or fewer")
    return name.strip()
