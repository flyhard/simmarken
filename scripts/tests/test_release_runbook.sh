#!/usr/bin/env bash
# Tests for scripts/lib/release-runbook.sh: status parsing, ticking rows,
# writing evidence, and refusing secret-looking evidence. Runs on a copy of the
# real runbook, so it never changes docs/ and never touches GitHub, 1Password
# or Gradle.
#
#   scripts/tests/test_release_runbook.sh
#
# Single quotes below are deliberate: they hold regexes, awk programs and
# literal backticks.
# shellcheck disable=SC2016
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

cp "$REPO_ROOT/docs/PLAY-CONSOLE-SETUP.md" "$WORK/runbook.md"
RUNBOOK="$WORK/runbook.md"
# shellcheck source=scripts/lib/release-runbook.sh
source "$REPO_ROOT/scripts/lib/release-runbook.sh"

FAILURES=0
pass() { printf 'ok   %s\n' "$1"; }
fail() { printf 'FAIL %s\n' "$1"; FAILURES=$((FAILURES + 1)); }
check() { # check NAME COMMAND…
  local name="$1"
  shift
  if "$@"; then pass "$name"; else fail "$name"; fi
}
refuses() { ! evidence_is_safe "$1" 2>/dev/null; }
fails() { ! "$@" 2>/dev/null; }

# --- status table --------------------------------------------------------
check "every step 2–10 has a status row" \
  bash -c 'for n in 2 3 4 5 6 7 8 9 10; do grep -Eq "^\| $n\. " "$0" || exit 1; done' "$RUNBOOK"
has_evidence_line() { # has_evidence_line N
  awk -v n="$1" '/^```/{c=!c} !c&&/^##? /{s=(index($0, "## " n ". ")==1)} s&&/^- Evidence:/{f=1} END{exit !f}' "$RUNBOOK"
}
all_steps_have_evidence() {
  local n
  for n in 2 3 4 5 6 7 8 9 10; do has_evidence_line "$n" || return 1; done
}
check "every step 2–10 has a section with an evidence line" all_steps_have_evidence

# Reset B and C rows so the test doesn't depend on how far the maintainer got.
_runbook_rewrite '/^\| ([2-9]|10)\. /{sub(/\| \[x\] \|[^|]*\|$/, "| [ ] | |")} {print}'
check "step 2 starts pending" bash -c '! grep -Eq "^\| 2\. .*\| \[x\] \|" "$0"' "$RUNBOOK"
check "runbook_pending lists steps 2..7" \
  test "$(runbook_pending 2 7 | tr '\n' ' ')" = "2 3 4 5 6 7 "

runbook_tick 2 2026-09-28
check "tick marks step 2 done" runbook_step_done 2
check "tick fills in the date" grep -Eq '^\| 2\. App record created \| RELE-02 \| \[x\] \| 2026-09-28 \|$' "$RUNBOOK"
check "tick leaves step 3 alone" bash -c '! grep -Eq "^\| 3\. .*\| \[x\] \|" "$0"' "$RUNBOOK"
check "runbook_pending skips ticked steps" \
  test "$(runbook_pending 2 7 | tr '\n' ' ')" = "3 4 5 6 7 "
runbook_tick 2 2026-09-29
check "re-ticking only updates the date" grep -Eq '^\| 2\. .*\| \[x\] \| 2026-09-29 \|$' "$RUNBOOK"
check "tick on a missing step fails" fails runbook_tick 42 2026-09-28

# --- evidence ------------------------------------------------------------
lines_before="$(wc -l <"$RUNBOOK")"
runbook_set_evidence 4 'upload-key SHA-256 matches, checked 2026-09-28 (fingerprint: `AB:CD:EF:01`)'
check "evidence replaces step 4's line" \
  grep -Fxq -- '- Evidence: upload-key SHA-256 matches, checked 2026-09-28 (fingerprint: `AB:CD:EF:01`)' "$RUNBOOK"
check "evidence drops the placeholder's continuation line" \
  bash -c '! grep -Fq "(fingerprint: \`AB:CD:…\`)" "$0"' "$RUNBOOK"
check "evidence leaves other sections alone" \
  grep -Fq -- '- Evidence: app created `YYYY-MM-DD`' "$RUNBOOK"
check "file shrank by exactly the continuation line" \
  test "$(wc -l <"$RUNBOOK")" -eq "$((lines_before - 1))"
check "evidence on a missing section fails" fails runbook_set_evidence 42 x

# --- secret guard --------------------------------------------------------
check "allows a date" evidence_is_safe '2026-09-28'
check "allows a URL" evidence_is_safe 'run https://github.com/flyhard/simmarken/actions/runs/36459968367 green'
check "allows a fingerprint" evidence_is_safe 'AB:CD:EF:01:23:45:67:89:AB:CD:EF:01:23:45:67:89:AB:CD:EF:01:23:45:67:89:AB:CD:EF:01:23:45:67:89'
check "allows a service-account email" evidence_is_safe 'play-publisher@simmarken-123456.iam.gserviceaccount.com'
check "allows empty evidence" evidence_is_safe ''
check "refuses a PEM block" refuses '-----BEGIN CERTIFICATE----- MIIE'
check "refuses private_key" refuses '{"private_key": "x"}'
check "refuses a 1Password reference" refuses 'op://Private/Android release keystore/store_password'
check "refuses a password" refuses 'Password: hunter2'
check "refuses a long opaque token" refuses 'key AIzaSyD3xampleExampleExample1234'
check "refuses several lines" refuses $'one\ntwo'

before="$(cat "$RUNBOOK")"
runbook_set_evidence 6 'secret created; key -----BEGIN CERTIFICATE-----' 2>/dev/null || true
runbook_tick 6 'password=x' 2>/dev/null || true
check "refused evidence leaves the runbook unchanged" test "$before" = "$(cat "$RUNBOOK")"

echo
if ((FAILURES)); then
  echo "$FAILURES test(s) failed"
  exit 1
fi
echo "All release-runbook tests passed."
