#!/usr/bin/env bash
# Manually distribute the exact signed APK already published on GitHub.
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo 'Usage: bash scripts/distribute_firebase_release.sh v0.1.1' >&2
  exit 2
fi

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
release_dir="$(mktemp -d "${TMPDIR:-/tmp}/morgenklar-distribution.XXXXXX")"
trap 'rm -rf -- "$release_dir"' EXIT

python3 "$script_dir/prepare_firebase_release.py" \
  --repo aPeterHeise/morgenklar --tag "$1" --output "$release_dir"

# Keep recipients in repository settings or the environment, outside source code.
testers="${FIREBASE_TESTERS:-$(gh variable get FIREBASE_TESTERS --repo aPeterHeise/morgenklar)}"
args=(appdistribution:distribute "$release_dir/morgenklar-${1#v}.apk"
  --app 1:166111565018:android:e9a8d6ab09ec29a4e581e3
  --project peter-heise-website-2026
  --release-notes-file "$release_dir/release-notes.txt" --non-interactive)
if [[ -n "$testers" ]]; then args+=(--testers "$testers"); fi
if [[ -n "${FIREBASE_GROUPS:-}" ]]; then args+=(--groups "$FIREBASE_GROUPS"); fi
npx -y firebase-tools@latest "${args[@]}"
