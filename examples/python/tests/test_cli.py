"""Tests for hello.cli module."""

import sys
from unittest.mock import patch

import pytest

from hello.cli import main


def test_main_prints_greeting(capsys: pytest.CaptureFixture[str]) -> None:
    with patch.object(sys, "argv", ["hello", "FOSS"]):
        main()
    assert "Hello, FOSS!" in capsys.readouterr().out


def test_main_default_greeting(capsys: pytest.CaptureFixture[str]) -> None:
    with patch.object(sys, "argv", ["hello"]):
        main()
    assert "Hello, world!" in capsys.readouterr().out


def test_main_invalid_name_exits(capsys: pytest.CaptureFixture[str]) -> None:
    with patch.object(sys, "argv", ["hello", "x" * 101]):
        with pytest.raises(SystemExit) as exc:
            main()
    assert exc.value.code == 1
    assert "Error:" in capsys.readouterr().err
