#!/usr/bin/env bash
# Phase 7 — security-git-hygiene automated verification
set -euo pipefail

SCRIPT_NAME="$(basename "$0")"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

usage() {
  cat <<EOF
Usage: $SCRIPT_NAME [OPTIONS]

Run automated verification checks for Phase 7 (security-git-hygiene).

Options:
  -h, --help          Show this help message
  -c, --check ID      Run a single check (e.g. 07-01-01)
  -l, --list          List available check IDs

Checks:
  07-01-01  Full-history Gitleaks scan (zero findings)
  07-01-02  Credential paths gitignored; none in git history
  07-02-01  Gitleaks workflow YAML valid with required triggers
  07-02-02  Latest gitleaks CI run on main branch is success
  07-03-01  SECURITY-CHECKLIST.md scaffold present
  07-03-02  All five checklist items marked complete

Manual checkpoints (not run by this script):
  07-01-03  History remediation gate (skipped — scan clean)
  07-02-03  Maintainer CI visual confirmation
  07-03-03  Maintainer sign-off on checklist

Examples:
  $SCRIPT_NAME                  # run all automated checks
  $SCRIPT_NAME --check 07-01-02
EOF
}

list_checks() {
  cat <<EOF
07-01-01
07-01-02
07-02-01
07-02-02
07-03-01
07-03-02
EOF
}

log_pass() {
  local id="$1"
  local description="$2"
  printf '==> [%s] %s ... PASS\n' "$id" "$description"
}

log_fail() {
  local id="$1"
  local message="$2"
  printf '==> [%s] FAIL: %s\n' "$id" "$message" >&2
}

check_07_01_01() {
  local id="07-01-01"
  if ! command -v gitleaks >/dev/null 2>&1; then
    log_fail "$id" "gitleaks not installed (brew install gitleaks)"
    return 1
  fi
  gitleaks detect --source . --config .gitleaks.toml --verbose
  log_pass "$id" "Full-history Gitleaks scan (zero findings)"
}

check_07_01_02() {
  local id="07-01-02"
  git check-ignore -v upload.jks keystore.properties play-credentials.json service-account.json local.properties

  local count
  count="$(git log --all --oneline -- '*.jks' '*.keystore' 'keystore.properties' '**/play-credentials.json' '**/service-account*.json' 'local.properties' | wc -l | tr -d '[:space:]')"
  if [[ "$count" != "0" ]]; then
    log_fail "$id" "expected 0 credential files in git history, got ${count}"
    return 1
  fi

  log_pass "$id" "Credential paths gitignored; none in git history"
}

check_07_02_01() {
  local id="07-02-01"
  ruby -ryaml -e "YAML.load_file('.github/workflows/gitleaks.yml')"
  grep -q 'fetch-depth: 0' .github/workflows/gitleaks.yml
  grep -q 'gitleaks/gitleaks-action@v3' .github/workflows/gitleaks.yml
  grep -q 'pull_request:' .github/workflows/gitleaks.yml
  grep -q 'push:' .github/workflows/gitleaks.yml
  log_pass "$id" "Gitleaks workflow YAML valid with required triggers"
}

check_07_02_02() {
  local id="07-02-02"
  if ! command -v gh >/dev/null 2>&1; then
    log_fail "$id" "gh CLI not installed"
    return 1
  fi

  local conclusion
  conclusion="$(gh run list --workflow=gitleaks.yml --branch main --limit 1 --json conclusion -q '.[0].conclusion' 2>/dev/null || true)"
  if [[ "$conclusion" != "success" ]]; then
    log_fail "$id" "latest main gitleaks run conclusion is '${conclusion:-<none>}', expected success"
    return 1
  fi

  log_pass "$id" "Latest gitleaks CI run on main branch is success"
}

check_07_03_01() {
  local id="07-03-01"
  test -f docs/SECURITY-CHECKLIST.md
  grep -q 'Pre-Public Security Checklist' docs/SECURITY-CHECKLIST.md
  grep -q 'Sign-Off' docs/SECURITY-CHECKLIST.md
  grep -q 'BackupDto' docs/SECURITY-CHECKLIST.md
  log_pass "$id" "SECURITY-CHECKLIST.md scaffold present"
}

check_07_03_02() {
  local id="07-03-02"
  local count
  count="$(grep -c '\[x\]' docs/SECURITY-CHECKLIST.md)"
  if [[ "$count" -lt 5 ]]; then
    log_fail "$id" "expected at least 5 checked items, got ${count}"
    return 1
  fi
  log_pass "$id" "All five checklist items marked complete"
}

run_check() {
  case "$1" in
    07-01-01) check_07_01_01 ;;
    07-01-02) check_07_01_02 ;;
    07-02-01) check_07_02_01 ;;
    07-02-02) check_07_02_02 ;;
    07-03-01) check_07_03_01 ;;
    07-03-02) check_07_03_02 ;;
    *)
      printf 'Unknown check ID: %s\n' "$1" >&2
      return 1
      ;;
  esac
}

run_all_checks() {
  local failed=0
  local check_id

  while IFS= read -r check_id; do
    if ! run_check "$check_id"; then
      failed=$((failed + 1))
    fi
  done < <(list_checks)

  echo
  if [[ "$failed" -eq 0 ]]; then
    printf 'All automated Phase 7 checks passed.\n'
    return 0
  fi

  printf '%d check(s) failed.\n' "$failed" >&2
  return 1
}

main() {
  local single_check=""

  while [[ $# -gt 0 ]]; do
    case "$1" in
      -h|--help)
        usage
        exit 0
        ;;
      -l|--list)
        list_checks
        exit 0
        ;;
      -c|--check)
        if [[ $# -lt 2 ]]; then
          printf '%s: --check requires an argument\n' "$SCRIPT_NAME" >&2
          exit 2
        fi
        single_check="$2"
        shift 2
        ;;
      *)
        printf '%s: unknown option: %s\n' "$SCRIPT_NAME" "$1" >&2
        usage >&2
        exit 2
        ;;
    esac
  done

  cd "$REPO_ROOT"

  if [[ -n "$single_check" ]]; then
    run_check "$single_check"
  else
    run_all_checks
  fi
}

main "$@"
