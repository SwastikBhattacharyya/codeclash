#!/usr/bin/env bash
set -uo pipefail
umask 022

readonly BIN_DIR=/sandbox/bin
readonly STDERR_FILE=/sandbox/run.stderr
readonly STDERR_MAX_BYTES=65536
readonly TIME_FILE=/sandbox/run.time
readonly MEMORY_EVENTS=/sys/fs/cgroup/memory.events
readonly OUTPUT_MAX_BYTES=16777216
readonly GRACE_MS=100
readonly MS_PER_SECOND=1000
readonly NS_PER_MS=1000000
readonly SIGNAL_OFFSET=128
readonly SIGXCPU_NUMBER=24
readonly SANDBOX_UID=10001
readonly SANDBOX_GID=10001
readonly EXIT_INTERNAL=2
readonly MEMORY_ERROR_PATTERN='java\.lang\.OutOfMemoryError: (Java heap space|GC overhead limit exceeded|Requested array size exceeds VM limit)|^MemoryError|std::bad_alloc'

usage() {
  echo "usage: run.sh <cpp|java|python> <time_limit_ms> <input_file> <output_file>" >&2
  exit "$EXIT_INTERNAL"
}

oom_kills() {
  awk '$1 == "oom_kill" { print $2; found = 1 } END { if (!found) print 0 }' "$MEMORY_EVENTS"
}

[[ $# -eq 4 ]] || usage
language=$1
time_limit_ms=$2
input_file=$3
output_file=$4
[[ $time_limit_ms =~ ^[1-9][0-9]*$ ]] || usage
[[ -f $input_file ]] || { echo "missing input file $input_file" >&2; exit "$EXIT_INTERNAL"; }
chmod go-w "$input_file" || exit "$EXIT_INTERNAL"

case $language in
  cpp)
    command=("$BIN_DIR/main")
    ;;
  java)
    command=(java -XX:+UseSerialGC -Xss64m -XX:MaxRAMPercentage=75 -XX:TieredStopAtLevel=1
      -XX:-UsePerfData -cp "$BIN_DIR" Main)
    ;;
  python)
    command=(python3 -I "$BIN_DIR/main.py")
    ;;
  *)
    usage
    ;;
esac

kill_after_ms=$((time_limit_ms + GRACE_MS))
kill_after=$(printf '%d.%03d' $((kill_after_ms / MS_PER_SECOND)) $((kill_after_ms % MS_PER_SECOND)))
cpu_soft_s=$(((time_limit_ms + MS_PER_SECOND - 1) / MS_PER_SECOND))
cpu_hard_s=$((cpu_soft_s + 1))

export HOME=/tmp
rm -f "$TIME_FILE" "$STDERR_FILE" "$output_file" || exit "$EXIT_INTERNAL"
find /tmp -mindepth 1 -delete 2>/dev/null
oom_before=$(oom_kills) || exit "$EXIT_INTERNAL"
start_ns=$(date +%s%N)
/usr/bin/time -f '%M' -o "$TIME_FILE" \
  timeout -s KILL "$kill_after" \
  prlimit --cpu="$cpu_soft_s:$cpu_hard_s" --fsize="$OUTPUT_MAX_BYTES" -- \
  setpriv --reuid="$SANDBOX_UID" --regid="$SANDBOX_GID" --clear-groups --no-new-privs -- \
  /usr/local/bin/sandbox-exec "${command[@]}" \
  < "$input_file" > "$output_file" 2> "$STDERR_FILE"
status=$?
end_ns=$(date +%s%N)
oom_after=$(oom_kills) || exit "$EXIT_INTERNAL"
truncate -s "<$STDERR_MAX_BYTES" "$STDERR_FILE"

elapsed_ms=$(((end_ns - start_ns) / NS_PER_MS))
memory_kb=$(tail -n 1 "$TIME_FILE" 2>/dev/null)
[[ $memory_kb =~ ^[0-9]+$ ]] || memory_kb=0

if ((oom_after > oom_before)); then
  verdict=MLE
elif ((elapsed_ms >= time_limit_ms)) || ((status == SIGNAL_OFFSET + SIGXCPU_NUMBER)); then
  verdict=TLE
elif ((status != 0)) && grep -Eq "$MEMORY_ERROR_PATTERN" "$STDERR_FILE"; then
  verdict=MLE
elif ((status != 0)); then
  verdict=RE
else
  verdict=OK
fi

echo "$verdict $elapsed_ms $memory_kb"
