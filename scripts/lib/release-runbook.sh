# shellcheck shell=bash
# Helpers for reading and updating the release runbook (docs/PLAY-CONSOLE-SETUP.md).
# Sourced by scripts/release-wizard.sh and scripts/tests/test_release_runbook.sh.
#
# The runbook is the wizard's only state: a step is done when its row in the
# status table reads "| N. … | [x] |". Nothing here touches GitHub, 1Password
# or Gradle, so it can be tested on its own.

RUNBOOK="${RUNBOOK:-docs/PLAY-CONSOLE-SETUP.md}"

# runbook_step_done N succeeds when step N is ticked in the status table.
runbook_step_done() {
  grep -Eq "^\| ${1}\. .*\| \[x\] \|" "$RUNBOOK"
}

# _runbook_rewrite AWK_PROGRAM [awk -v args…] rewrites the runbook through awk.
# Writes to a temp file next to the runbook and moves it back, so it behaves
# the same with GNU and BSD tools (no sed -i).
_runbook_rewrite() {
  local program="$1" tmp
  shift
  tmp="$(mktemp "${RUNBOOK}.XXXXXX")"
  if awk "$@" "$program" "$RUNBOOK" >"$tmp"; then
    mv "$tmp" "$RUNBOOK"
  else
    rm -f "$tmp"
    return 1
  fi
}

# runbook_tick N DATE ticks step N's status row and fills in its date.
runbook_tick() {
  local step="$1" date="$2"
  evidence_is_safe "$date" || return 1
  grep -Eq "^\| ${step}\. " "$RUNBOOK" || {
    echo "runbook: no status row for step $step" >&2
    return 1
  }
  # shellcheck disable=SC2016 # awk program, not shell
  _runbook_rewrite '
    index($0, "| " step ". ") == 1 {
      n = split($0, cells, "|")
      # cells: "" | step | requirement | done | date | ""
      cells[4] = " [x] "
      cells[5] = " " date " "
      line = ""
      for (i = 2; i < n; i++) line = line "|" cells[i]
      print line "|"
      next
    }
    { print }
  ' -v step="$step" -v date="$date"
}

# runbook_set_evidence N TEXT replaces the "- Evidence:" line of section
# "## N. …" (and its indented continuation lines) with "- Evidence: TEXT".
runbook_set_evidence() {
  local step="$1" text="$2"
  evidence_is_safe "$text" || return 1
  grep -Eq "^## ${step}\. " "$RUNBOOK" || {
    echo "runbook: no section for step $step" >&2
    return 1
  }
  # shellcheck disable=SC2016 # awk program, not shell
  _runbook_rewrite '
    /^```/ { in_code = !in_code }
    !in_code && /^##? / { in_section = (index($0, "## " step ". ") == 1) }
    skipping && /^  / { next }
    { skipping = 0 }
    in_section && !done && /^- Evidence:/ {
      print "- Evidence: " text
      done = 1
      skipping = 1
      next
    }
    { print }
  ' -v step="$step" -v text="$text"
}

# evidence_is_safe TEXT fails (and says why) when TEXT looks like it could be
# a secret. Evidence is committed to a public repository: dates, URLs, release
# IDs, emails and certificate fingerprints are fine; keys and passwords are not.
evidence_is_safe() {
  local text="$1" lower word words
  lower="$(printf '%s' "$text" | tr '[:upper:]' '[:lower:]')"
  case "$text" in
    *$'\n'*) _unsafe "it spans several lines" ; return 1 ;;
  esac
  case "$lower" in
    *"-----begin"*) _unsafe "it contains a PEM block" ; return 1 ;;
    *private_key*) _unsafe "it mentions private_key" ; return 1 ;;
    *op://*) _unsafe "it contains a 1Password reference" ; return 1 ;;
    *password*|*passwd*|*secret=*|*token=*) _unsafe "it looks like a password" ; return 1 ;;
  esac
  # Long opaque tokens (API keys, base64 blobs) mix letters and digits with no
  # separators. Fingerprints use colons and URLs use slashes, so they pass.
  [[ -n "$text" ]] || return 0
  read -ra words <<<"$text"
  for word in "${words[@]}"; do
    if [[ ${#word} -ge 24 && "$word" =~ ^[A-Za-z0-9+=_-]+$ \
          && "$word" =~ [A-Za-z] && "$word" =~ [0-9] ]]; then
      _unsafe "it contains a long opaque token"
      return 1
    fi
  done
}

_unsafe() {
  echo "runbook: refusing to write evidence because $1." >&2
}

# runbook_pending FIRST LAST prints the step numbers in FIRST..LAST that are not ticked.
runbook_pending() {
  local step
  for ((step = $1; step <= $2; step++)); do
    runbook_step_done "$step" || printf '%s\n' "$step"
  done
}
