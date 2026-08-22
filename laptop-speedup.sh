#!/usr/bin/env bash
# laptop-speedup.sh — diagnose slowness and apply safe, reversible cleanups.
#
# This does not overclock hardware, disable security tools, or kill random
# processes. It reports resource hogs and, with confirmation, frees disk
# space from caches and trash.
set -euo pipefail

DRY_RUN=0
ASSUME_YES=0
DO_CLEAN=0

usage() {
  cat <<'EOF'
Usage: ./laptop-speedup.sh [options]

Options:
  --diagnose     Print CPU, memory, disk, and top process stats (default)
  --clean        After diagnosing, offer to clear user caches and trash
  --yes          Skip confirmation prompts (use with --clean)
  --dry-run      Show what --clean would remove without deleting anything
  -h, --help     Show this help

Examples:
  ./laptop-speedup.sh
  ./laptop-speedup.sh --clean
  ./laptop-speedup.sh --clean --dry-run
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --diagnose) shift ;;
    --clean) DO_CLEAN=1; shift ;;
    --yes|-y) ASSUME_YES=1; shift ;;
    --dry-run) DRY_RUN=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

OS="$(uname -s)"
USER_HOME="${HOME}"

hr() { printf '%s\n' "----------------------------------------"; }

section() {
  printf '\n'
  hr
  printf '%s\n' "$1"
  hr
}

bytes_human() {
  local n="${1:-0}"
  if command -v numfmt >/dev/null 2>&1; then
    numfmt --to=iec --suffix=B "$n" 2>/dev/null || echo "${n}B"
  else
    echo "${n}B"
  fi
}

dir_size_bytes() {
  local path="$1"
  if [[ -d "$path" ]]; then
    du -sb "$path" 2>/dev/null | awk '{print $1}'
  else
    echo 0
  fi
}

print_header() {
  section "Laptop check — $(date)"
  echo "User:     ${USER:-unknown}"
  echo "Host:     $(hostname 2>/dev/null || echo unknown)"
  echo "OS:       ${OS} $(uname -r)"
  if command -v uptime >/dev/null 2>&1; then
    echo "Uptime:   $(uptime | sed 's/^ *//')"
  fi
}

print_cpu() {
  section "CPU load"
  if [[ -r /proc/loadavg ]]; then
    read -r l1 l5 l15 _ </proc/loadavg
    cores="$(getconf _NPROCESSORS_ONLN 2>/dev/null || echo 1)"
    echo "Load average: ${l1} ${l5} ${l15}  (cores: ${cores})"
    awk -v c="$cores" -v l="$l1" 'BEGIN {
      if (c+0 == 0) c=1
      r = l/c
      if (r >= 1.5) print "Hint: load is high vs core count; check top processes below."
      else if (r >= 0.8) print "Hint: CPU is fairly busy."
      else print "Hint: CPU load looks OK."
    }'
  elif command -v sysctl >/dev/null 2>&1; then
    echo "Load: $(sysctl -n vm.loadavg 2>/dev/null || true)"
  else
    echo "Could not read load average."
  fi
}

print_memory() {
  section "Memory"
  if [[ -r /proc/meminfo ]]; then
    awk '
      /MemTotal:/     { t=$2 }
      /MemAvailable:/ { a=$2 }
      END {
        used = t - a
        printf "Total:     %.1f GiB\n", t/1024/1024
        printf "Available: %.1f GiB\n", a/1024/1024
        printf "In use:    %.1f GiB (%.0f%%)\n", used/1024/1024, (used/t)*100
        if (a/t < 0.10) print "Hint: little free RAM; close unused apps or add swap."
        else if (a/t < 0.20) print "Hint: RAM is getting tight."
        else print "Hint: memory pressure looks OK."
      }
    ' /proc/meminfo
    if [[ -r /proc/swaps ]]; then
      awk 'NR>1 {s+=$3; u+=$4} END {
        if (NR<=1) { print "Swap:      none configured"; exit }
        printf "Swap used: %.1f GiB of %.1f GiB\n", u/1024/1024, s/1024/1024
        if (s>0 && u/s > 0.5) print "Hint: heavy swap use often feels like a slow laptop."
      }' /proc/swaps
    fi
  elif command -v vm_stat >/dev/null 2>&1; then
    vm_stat | head -n 20
  else
    free -h 2>/dev/null || echo "Could not read memory stats."
  fi
}

print_disk() {
  section "Disk space (local filesystems)"
  df -h -x tmpfs -x devtmpfs -x squashfs 2>/dev/null || df -h
  echo
  if command -v df >/dev/null 2>&1; then
    df -P -x tmpfs -x devtmpfs 2>/dev/null | awk 'NR>1 {
      gsub(/%/,"",$5)
      if ($5+0 >= 90) print "Hint: "$6" is "$5"% full; low disk space slows the system."
    }'
  fi
}

print_top_cpu() {
  section "Top CPU processes"
  if command -v ps >/dev/null 2>&1; then
    ps -eo pid,pcpu,pmem,comm --sort=-pcpu 2>/dev/null | head -n 11 \
      || ps aux | sort -nrk 3 | head -n 11
  fi
}

print_top_mem() {
  section "Top memory processes"
  if command -v ps >/dev/null 2>&1; then
    ps -eo pid,pcpu,pmem,comm --sort=-pmem 2>/dev/null | head -n 11 \
      || ps aux | sort -nrk 4 | head -n 11
  fi
}

print_tips() {
  section "What usually helps"
  cat <<'EOF'
- Close unused browser tabs and Electron apps (Slack, Discord, VS Code windows).
- Reboot if uptime is many days and RAM/swap look exhausted.
- Keep at least 10–15% of the system disk free.
- On Linux laptops, a plugged-in AC adapter often uses a higher CPU governor.
- Update the OS and drivers; thermal dust-cleaning helps more than scripts.
EOF
}

# Candidate cleanup paths that belong to the current user only.
cleanup_targets() {
  local -a paths=()
  paths+=(
    "${USER_HOME}/.cache"
    "${USER_HOME}/.thumbnails"
    "${USER_HOME}/.local/share/Trash"
    "${USER_HOME}/.Trash"
  )
  # Common browser/dev caches (safe to regenerate)
  [[ -d "${USER_HOME}/.npm/_cacache" ]] && paths+=("${USER_HOME}/.npm/_cacache")
  [[ -d "${USER_HOME}/.cache/pip" ]] && paths+=("${USER_HOME}/.cache/pip")
  [[ -d "${USER_HOME}/Library/Caches" ]] && paths+=("${USER_HOME}/Library/Caches")
  [[ -d "${USER_HOME}/Library/Logs" ]] && paths+=("${USER_HOME}/Library/Logs")

  printf '%s\n' "${paths[@]}" | awk 'NF && !seen[$0]++'
}

confirm() {
  local prompt="$1"
  if [[ "$ASSUME_YES" -eq 1 ]]; then
    return 0
  fi
  read -r -p "${prompt} [y/N] " reply
  [[ "$reply" =~ ^[Yy]$ ]]
}

do_clean() {
  section "Safe cleanup (user caches and trash)"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "Dry run: nothing will be deleted."
  fi

  local total=0
  local path size
  while IFS= read -r path; do
    [[ -e "$path" ]] || continue
    size="$(dir_size_bytes "$path")"
    total=$((total + size))
    printf '  %s  (%s)\n' "$path" "$(bytes_human "$size")"
  done < <(cleanup_targets)

  echo
  echo "Estimated reclaimable (user-owned): $(bytes_human "$total")"

  if [[ "$DRY_RUN" -eq 1 ]]; then
    return 0
  fi

  if ! confirm "Delete the listed user caches and trash?"; then
    echo "Skipped cleanup."
    return 0
  fi

  while IFS= read -r path; do
    [[ -e "$path" ]] || continue
    # Only remove contents, keep the directory itself.
    find "$path" -mindepth 1 -maxdepth 1 -exec rm -rf {} + 2>/dev/null || true
    echo "Cleared: $path"
  done < <(cleanup_targets)

  if command -v sync >/dev/null 2>&1; then
    sync
  fi
  echo "Cleanup finished. A reboot can still help if RAM was exhausted."
}

print_header
print_cpu
print_memory
print_disk
print_top_cpu
print_top_mem
print_tips

if [[ "$DO_CLEAN" -eq 1 ]]; then
  do_clean
else
  section "Next step"
  echo "Re-run with --clean to clear user caches and trash after review:"
  echo "  $0 --clean"
  echo "Preview deletions with:"
  echo "  $0 --clean --dry-run"
fi
