"""CLI entry point for Golden Path Python stub."""

import argparse
import sys

from hello.greet import greet, validate_name


def main() -> None:
    """Run the hello CLI."""
    parser = argparse.ArgumentParser(description="Golden Path Python CLI stub")
    parser.add_argument("name", nargs="?", default="", help="Name to greet")
    args = parser.parse_args()

    try:
        validated = validate_name(args.name)
        print(greet(validated))
    except ValueError as exc:
        print(f"Error: {exc}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
