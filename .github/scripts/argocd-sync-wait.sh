#!/usr/bin/env bash
# Syncs an ArgoCD application and waits until it is Synced to <revision> and Healthy.
# usage: argocd-sync-wait.sh <application> <revision>   (env: ARGOCD_SERVER_URL, ARGOCD_TOKEN)
set -euo pipefail
app=$1 revision=$2
timeout=${TIMEOUT:-600} interval=${INTERVAL:-15} required=3
api() { curl -fsS -H "Authorization: Bearer $ARGOCD_TOKEN" "$@"; }

# The ApplicationSet creates the application on its next git poll, so a new service may not exist yet.
# 404 and 403 both mean "not there yet": ArgoCD answers 403 for a missing application unless the caller may read all of them.
# Any other status (401 bad token, wrong URL, ...) is a real error; a 403 that never turns into 200 is reported at the timeout.
elapsed=0
while :; do
  code=$(curl -sS -o /tmp/argocd-app.json -w '%{http_code}' -H "Authorization: Bearer $ARGOCD_TOKEN" \
    "$ARGOCD_SERVER_URL/api/v1/applications/$app")
  [ "$code" = 200 ] && break
  if [ "$code" != 404 ] && [ "$code" != 403 ]; then
    echo "::error::GET $ARGOCD_SERVER_URL/api/v1/applications/$app returned HTTP $code: $(head -c 300 /tmp/argocd-app.json)"
    exit 1
  fi
  if [ $elapsed -ge "${APPEAR_TIMEOUT:-300}" ]; then
    echo "::error::application $app did not become visible (last HTTP $code: $(head -c 300 /tmp/argocd-app.json)). Either the ApplicationSet has not created it, or the token lacks 'applications, get' permission."
    exit 1
  fi
  echo "Application $app not visible yet (HTTP $code); waiting for the ApplicationSet..."
  sleep "${APPEAR_INTERVAL:-10}"; elapsed=$((elapsed + ${APPEAR_INTERVAL:-10}))
done

api "$ARGOCD_SERVER_URL/api/v1/applications/$app?refresh=hard" > /dev/null
# Auto-sync may already be running; a rejected manual sync is fine then.
api -X POST -H "Content-Type: application/json" -d '{}' "$ARGOCD_SERVER_URL/api/v1/applications/$app/sync" > /dev/null \
  || echo "Manual sync was rejected (probably already syncing); continuing"

ok=0 elapsed=0
while [ $elapsed -lt "$timeout" ]; do
  r=$(api "$ARGOCD_SERVER_URL/api/v1/applications/$app")
  sync=$(jq -r '.status.sync.status' <<< "$r") rev=$(jq -r '.status.sync.revision' <<< "$r")
  health=$(jq -r '.status.health.status' <<< "$r") op=$(jq -r '.status.operationState.phase // ""' <<< "$r")
  echo "sync=$sync revision=${rev:0:7} health=$health operation=$op (${elapsed}s)"
  if [ "$op" = "Failed" ] || [ "$op" = "Error" ]; then echo "::error::ArgoCD operation $op"; exit 1; fi
  if [ "$sync" = "Synced" ] && [ "$health" = "Healthy" ] && [ "$rev" = "$revision" ]; then ok=$((ok + 1)); else ok=0; fi
  if [ $ok -ge $required ]; then echo "$app is synced to ${revision:0:7} and healthy"; exit 0; fi
  sleep "$interval"; elapsed=$((elapsed + interval))
done
echo "::error::$app was not healthy at ${revision:0:7} within ${timeout}s"
exit 1
