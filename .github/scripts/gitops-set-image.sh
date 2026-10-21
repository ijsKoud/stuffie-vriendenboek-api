#!/usr/bin/env bash
# Points the service's image in the gitops checkout at the new version.
# usage: gitops-set-image.sh <gitops-checkout> <service-dir> <image> <image-ref> <version> <rollback>
# Fails when the manifests are missing; sets output skip=true when gitops already holds a newer version.
set -euo pipefail
checkout=$1 dir=$2 image=$3 ref=$4 version=$5 rollback=$6
target="$checkout/$dir"

[ -d "$target" ] || { echo "::error::$dir does not exist in homelab-gitops; add the manifests before releasing this service"; exit 1; }

files=$(grep -rlE "image: *${image}[:@]" "$target" --include='*.yaml' --include='*.yml' || true)
[ -n "$files" ] || { echo "::error::no 'image: $image' line found under $dir"; exit 1; }

current=$(grep -rhoE "image: *${image}:[0-9]+\.[0-9]+\.[0-9]+" $files | head -1 | sed -E 's/.*:([0-9.]+)$/\1/' || true)
if [ -n "$current" ] && [ "$rollback" != "true" ] && [ "$current" != "$version" ] \
  && [ "$(printf '%s\n%s\n' "$current" "$version" | sort -V | tail -1)" = "$current" ]; then
  echo "::notice::gitops already has $current, which is newer than $version; skipping (use the rollback option to go back)"
  echo "skip=true" >> "$GITHUB_OUTPUT"
  exit 0
fi

# shellcheck disable=SC2086
sed -i.bak -E "s|(image: *)${image}[:@][^[:space:]]*|\1${ref}|" $files
find "$target" -name '*.bak' -delete
echo "skip=false" >> "$GITHUB_OUTPUT"
