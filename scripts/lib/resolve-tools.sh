# Prepend Windows Git / gh / node so Git Bash finds them.
# Drop inherited PYTHONPATH so scripts/lib does not shadow repo-root imports.
# shellcheck shell=bash
_add_path() {
  [ -d "$1" ] || return 0
  case ":$PATH:" in
    *":$1:"*) ;;
    *) PATH="$1:$PATH" ;;
  esac
}

unset PYTHONPATH

if [ "${OS:-}" = "Windows_NT" ] || uname -s 2>/dev/null | grep -qiE 'mingw|msys|cygwin'; then
  _add_path "/c/Program Files/GitHub CLI"
  _add_path "/c/Program Files/Git/bin"
  _add_path "/c/Program Files/nodejs"
  _add_path "/c/Program Files (x86)/Git/bin"
  if [ -n "${LOCALAPPDATA:-}" ]; then
    _lad="${LOCALAPPDATA//\\//}"
    case "$_lad" in
      [A-Za-z]:*) _lad="/$(printf '%s' "${_lad:0:1}" | tr '[:upper:]' '[:lower:]')${_lad:2}" ;;
    esac
    _add_path "$_lad/Programs/GitHub CLI"
  fi
fi
export PATH
unset _lad
