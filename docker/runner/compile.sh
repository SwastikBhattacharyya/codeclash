#!/usr/bin/env bash
set -uo pipefail
umask 022

readonly SRC_DIR=/sandbox/src
readonly BIN_DIR=/sandbox/bin
readonly LOG_FILE=/sandbox/compile.log
readonly LOG_MAX_BYTES=65536
readonly TRUNCATION_MARKER=$'\n\xe2\x80\xa6 output truncated\n'
readonly SANDBOX_UID=10001
readonly SANDBOX_GID=10001
readonly EXIT_COMPILED=0
readonly EXIT_COMPILATION_ERROR=1
readonly EXIT_INTERNAL=2
readonly EXIT_TIMEOUT_FAILED=125
readonly EXIT_NOT_EXECUTABLE=126
readonly EXIT_NOT_FOUND=127
readonly PYTHON_SYNTAX_CHECK='import sys; compile(open(sys.argv[1], "rb").read(), sys.argv[1], "exec")'

usage() {
  echo "usage: compile.sh <cpp|java|python> <time_limit_s>" >&2
  exit "$EXIT_INTERNAL"
}

[[ $# -eq 2 ]] || usage
language=$1
time_limit_s=$2
[[ $time_limit_s =~ ^[1-9][0-9]*$ ]] || usage

case $language in
  cpp)
    source_file=$SRC_DIR/Main.cpp
    command=(g++ -std=c++20 -O2 -pipe -o "$BIN_DIR/main" "$source_file")
    ;;
  java)
    source_file=$SRC_DIR/Main.java
    command=(javac -J-XX:+UseSerialGC -J-XX:-UsePerfData -d "$BIN_DIR" "$source_file")
    ;;
  python)
    source_file=$SRC_DIR/Main.py
    command=(python3 -I -c "$PYTHON_SYNTAX_CHECK" "$source_file")
    ;;
  *)
    usage
    ;;
esac

[[ -f $source_file ]] || { echo "missing source file $source_file" >&2; exit "$EXIT_INTERNAL"; }
chmod -R go-w "$SRC_DIR" || exit "$EXIT_INTERNAL"
export HOME=/tmp
rm -rf "$BIN_DIR" && mkdir -p "$BIN_DIR" && chown "$SANDBOX_UID:$SANDBOX_GID" "$BIN_DIR" \
  || exit "$EXIT_INTERNAL"

timeout -s KILL "$time_limit_s" \
  setpriv --reuid="$SANDBOX_UID" --regid="$SANDBOX_GID" --clear-groups --no-new-privs -- \
  "${command[@]}" 2>&1 | { head -c "$((LOG_MAX_BYTES + 1))" > "$LOG_FILE"; cat > /dev/null; }
status=${PIPESTATUS[0]}

if [[ $(stat -c %s "$LOG_FILE") -gt $LOG_MAX_BYTES ]]; then
  marker_bytes=$(printf '%s' "$TRUNCATION_MARKER" | wc -c)
  truncate -s "$((LOG_MAX_BYTES - marker_bytes))" "$LOG_FILE" || exit "$EXIT_INTERNAL"
  printf '%s' "$TRUNCATION_MARKER" >> "$LOG_FILE" || exit "$EXIT_INTERNAL"
fi

if [[ $status -eq $EXIT_TIMEOUT_FAILED || $status -eq $EXIT_NOT_EXECUTABLE \
  || $status -eq $EXIT_NOT_FOUND ]]; then
  exit "$EXIT_INTERNAL"
fi
if [[ $status -ne 0 ]]; then
  cat "$LOG_FILE"
  exit "$EXIT_COMPILATION_ERROR"
fi
chown -R 0:0 "$BIN_DIR" || exit "$EXIT_INTERNAL"
if [[ $language == python ]]; then
  cp "$source_file" "$BIN_DIR/main.py" || exit "$EXIT_INTERNAL"
fi
chmod -R a-w "$BIN_DIR" || exit "$EXIT_INTERNAL"
exit "$EXIT_COMPILED"
