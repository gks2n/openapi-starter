#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
DEFAULT_PASSWORD="${APP_USER_DEFAULT_PASSWORD:-password123}"

tmpdir="$(mktemp -d)"
cleanup() {
  rm -rf "$tmpdir"
}
trap cleanup EXIT

failures=0

request() {
  local method="$1"
  local path="$2"
  local data="${3:-}"
  local token="${4:-}"
  local body_file="$tmpdir/body.json"
  local headers=()
  if [[ -n "$token" ]]; then
    headers+=(-H "Authorization: Bearer $token")
  fi
  if [[ -n "$data" ]]; then
    headers+=(-H "Content-Type: application/json")
    status="$(curl -sS -o "$body_file" -w "%{http_code}" -X "$method" "${headers[@]}" \
      "$BASE_URL$path" -d "$data")"
  else
    status="$(curl -sS -o "$body_file" -w "%{http_code}" -X "$method" "${headers[@]}" \
      "$BASE_URL$path")"
  fi
  echo "$status"
}

json_get() {
  local file="$1"
  local key="$2"
  python3 - "$file" "$key" <<'PY'
import json
import sys

path = sys.argv[2].split(".")
with open(sys.argv[1], "r", encoding="utf-8") as fh:
    data = json.load(fh)
for part in path:
    if isinstance(data, dict):
        data = data.get(part)
    else:
        data = None
        break
print("" if data is None else data)
PY
}

jwt_claim() {
  local token="$1"
  local claim="$2"
  python3 - "$token" "$claim" <<'PY'
import base64
import json
import sys

token = sys.argv[1].strip()
claim = sys.argv[2]
parts = token.split(".")
if len(parts) < 2:
    print("")
    sys.exit(0)
payload = parts[1]
payload += "=" * (-len(payload) % 4)
data = base64.urlsafe_b64decode(payload.encode("utf-8"))
obj = json.loads(data.decode("utf-8"))
print(obj.get(claim, ""))
PY
}

assert_status() {
  local name="$1"
  local actual="$2"
  local expected="$3"
  if [[ "$actual" == "$expected" ]]; then
    echo "PASS: $name ($actual)"
  else
    echo "FAIL: $name (expected $expected, got $actual)"
    failures=$((failures + 1))
  fi
}

echo "Base URL: $BASE_URL"

echo "Scenario: Create a new user"
email_suffix="$(date +%s)"
auth_email="scenario+$email_suffix@example.com"
create_user_payload="$(cat <<JSON
{
  "name": "Scenario User",
  "address": {
    "line1": "1 High Street",
    "town": "London",
    "county": "Greater London",
    "postcode": "SW1A 1AA"
  },
  "phoneNumber": "+447700900999",
  "email": "$auth_email"
}
JSON
)"
status="$(request POST "/v1/users" "$create_user_payload")"
assert_status "Create user" "$status" "201"

echo "Scenario: Create a new user missing data"
status="$(request POST "/v1/users" '{"name":"Missing Required"}')"
assert_status "Create user missing data" "$status" "400"

echo "Scenario: Authenticate a user"
auth_payload="$(cat <<JSON
{"email":"$auth_email","password":"$DEFAULT_PASSWORD"}
JSON
)"
status="$(request POST "/v1/auth/login" "$auth_payload")"
assert_status "Authenticate user" "$status" "200"
token="$(json_get "$tmpdir/body.json" "accessToken")"
if [[ -z "$token" ]]; then
  echo "FAIL: Missing access token in auth response"
  exit 1
fi
current_user_id="$(jwt_claim "$token" "userId")"
if [[ -z "$current_user_id" ]]; then
  echo "FAIL: Missing userId claim in token"
  exit 1
fi

echo "Scenario: Create another user for forbidden tests"
other_email="other+$email_suffix@example.com"
other_user_payload="$(cat <<JSON
{
  "name": "Other User",
  "address": {
    "line1": "2 High Street",
    "town": "London",
    "county": "Greater London",
    "postcode": "SW1A 1AA"
  },
  "phoneNumber": "+447700900998",
  "email": "$other_email"
}
JSON
)"
status="$(request POST "/v1/users" "$other_user_payload")"
assert_status "Create other user" "$status" "201"

echo "Scenario: Authenticate other user"
other_auth_payload="$(cat <<JSON
{"email":"$other_email","password":"$DEFAULT_PASSWORD"}
JSON
)"
status="$(request POST "/v1/auth/login" "$other_auth_payload")"
assert_status "Authenticate other user" "$status" "200"
other_token="$(json_get "$tmpdir/body.json" "accessToken")"
if [[ -z "$other_token" ]]; then
  echo "FAIL: Missing access token for other user"
  exit 1
fi
other_user_id="$(jwt_claim "$other_token" "userId")"
if [[ -z "$other_user_id" ]]; then
  echo "FAIL: Missing userId claim for other user"
  exit 1
fi

echo "Scenario: Fetch user details (self)"
status="$(request GET "/v1/users/$current_user_id" "" "$token")"
assert_status "Fetch own user" "$status" "200"

echo "Scenario: Fetch user details of another user"
status="$(request GET "/v1/users/$other_user_id" "" "$token")"
assert_status "Fetch other user" "$status" "403"

echo "Scenario: Update user details (self)"
status="$(request PATCH "/v1/users/$current_user_id" '{"name":"Updated Name"}' "$token")"
assert_status "Update own user" "$status" "200"

echo "Scenario: Update user details of another user"
status="$(request PATCH "/v1/users/$other_user_id" '{"name":"Nope"}' "$token")"
assert_status "Update other user" "$status" "403"

echo "Scenario: Create a bank account"
status="$(request POST "/v1/accounts" '{"name":"Scenario Account","accountType":"personal"}' "$token")"
assert_status "Create account" "$status" "201"
account_tx="$(json_get "$tmpdir/body.json" "accountNumber")"

echo "Scenario: Create another bank account (for wrong-account tests)"
status="$(request POST "/v1/accounts" '{"name":"Scenario Account 2","accountType":"personal"}' "$token")"
assert_status "Create second account" "$status" "201"
account_other="$(json_get "$tmpdir/body.json" "accountNumber")"

echo "Scenario: Create other user's account"
status="$(request POST "/v1/accounts" '{"name":"Other Account","accountType":"personal"}' "$other_token")"
assert_status "Create other account" "$status" "201"
other_account_id="$(json_get "$tmpdir/body.json" "accountNumber")"

echo "Scenario: Create bank account missing data"
status="$(request POST "/v1/accounts" '{"name":"Missing Type"}' "$token")"
assert_status "Create account missing data" "$status" "400"

echo "Scenario: List bank accounts"
status="$(request GET "/v1/accounts" "" "$token")"
assert_status "List accounts" "$status" "200"

echo "Scenario: Fetch bank account details (self)"
status="$(request GET "/v1/accounts/$account_tx" "" "$token")"
assert_status "Fetch own account" "$status" "200"

echo "Scenario: Fetch another user's bank account"
status="$(request GET "/v1/accounts/$other_account_id" "" "$token")"
assert_status "Fetch other account" "$status" "403"

echo "Scenario: Fetch non-existent bank account"
status="$(request GET "/v1/accounts/01000000" "" "$token")"
assert_status "Fetch missing account" "$status" "404"

echo "Scenario: Update bank account details (self)"
status="$(request PATCH "/v1/accounts/$account_tx" '{"name":"Updated Account Name"}' "$token")"
assert_status "Update own account" "$status" "200"

echo "Scenario: Update another user's bank account"
status="$(request PATCH "/v1/accounts/$other_account_id" '{"name":"Nope"}' "$token")"
assert_status "Update other account" "$status" "403"

echo "Scenario: Update non-existent bank account"
status="$(request PATCH "/v1/accounts/01000000" '{"name":"Nope"}' "$token")"
assert_status "Update missing account" "$status" "404"

echo "Scenario: Deposit into own account"
status="$(request POST "/v1/accounts/$account_tx/transactions" \
  '{"amount":100.00,"currency":"GBP","type":"deposit","reference":"Top up"}' "$token")"
assert_status "Deposit" "$status" "201"
transaction_id="$(json_get "$tmpdir/body.json" "id")"

echo "Scenario: Withdraw from own account"
status="$(request POST "/v1/accounts/$account_tx/transactions" \
  '{"amount":50.00,"currency":"GBP","type":"withdrawal","reference":"ATM"}' "$token")"
assert_status "Withdrawal" "$status" "201"

echo "Scenario: Withdraw with insufficient funds"
status="$(request POST "/v1/accounts/$account_tx/transactions" \
  '{"amount":9999.00,"currency":"GBP","type":"withdrawal","reference":"Big ATM"}' "$token")"
assert_status "Withdrawal insufficient funds" "$status" "422"

echo "Scenario: Deposit/withdraw on another user's account"
status="$(request POST "/v1/accounts/$other_account_id/transactions" \
  '{"amount":10.00,"currency":"GBP","type":"deposit"}' "$token")"
assert_status "Transaction on other account" "$status" "403"

echo "Scenario: Deposit/withdraw on non-existent account"
status="$(request POST "/v1/accounts/01000000/transactions" \
  '{"amount":10.00,"currency":"GBP","type":"deposit"}' "$token")"
assert_status "Transaction on missing account" "$status" "404"

echo "Scenario: Deposit/withdraw missing data"
status="$(request POST "/v1/accounts/$account_tx/transactions" '{"amount":10.00}' "$token")"
assert_status "Transaction missing data" "$status" "400"

echo "Scenario: List transactions (self)"
status="$(request GET "/v1/accounts/$account_tx/transactions" "" "$token")"
assert_status "List transactions" "$status" "200"

echo "Scenario: List transactions on another user's account"
status="$(request GET "/v1/accounts/$other_account_id/transactions" "" "$token")"
assert_status "List other transactions" "$status" "403"

echo "Scenario: List transactions on non-existent account"
status="$(request GET "/v1/accounts/01000000/transactions" "" "$token")"
assert_status "List missing transactions" "$status" "404"

echo "Scenario: Fetch transaction on own account"
status="$(request GET "/v1/accounts/$account_tx/transactions/$transaction_id" "" "$token")"
assert_status "Fetch own transaction" "$status" "200"

echo "Scenario: Fetch transaction on another user's account"
status="$(request GET "/v1/accounts/$other_account_id/transactions/$transaction_id" "" "$token")"
assert_status "Fetch transaction other account" "$status" "403"

echo "Scenario: Fetch transaction on non-existent account"
status="$(request GET "/v1/accounts/01000000/transactions/$transaction_id" "" "$token")"
assert_status "Fetch transaction missing account" "$status" "404"

echo "Scenario: Fetch non-existent transaction"
status="$(request GET "/v1/accounts/$account_tx/transactions/tan-doesnotexist" "" "$token")"
assert_status "Fetch missing transaction" "$status" "404"

echo "Scenario: Fetch transaction against wrong account (same user)"
status="$(request GET "/v1/accounts/$account_other/transactions/$transaction_id" "" "$token")"
assert_status "Fetch transaction wrong account" "$status" "404"

echo "Scenario: Delete another user's bank account"
status="$(request DELETE "/v1/accounts/$other_account_id" "" "$token")"
assert_status "Delete other account" "$status" "403"

echo "Scenario: Delete non-existent bank account"
status="$(request DELETE "/v1/accounts/01000000" "" "$token")"
assert_status "Delete missing account" "$status" "404"

echo "Scenario: Delete own bank account"
status="$(request DELETE "/v1/accounts/$account_other" "" "$token")"
assert_status "Delete own account" "$status" "204"

echo "Scenario: Delete user details when accounts exist"
status="$(request DELETE "/v1/users/$current_user_id" "" "$token")"
assert_status "Delete user with accounts" "$status" "409"

echo "Scenario: Delete user details after removing accounts"
status="$(request DELETE "/v1/accounts/$account_tx" "" "$token")"
assert_status "Delete last account" "$status" "204"

status="$(request DELETE "/v1/users/$current_user_id" "" "$token")"
assert_status "Delete user without accounts" "$status" "204"

echo "Scenario: Delete non-existent user"
status="$(request DELETE "/v1/users/$current_user_id" "" "$token")"
assert_status "Delete missing user" "$status" "404"

echo "Scenario: Fetch non-existent user (after deletion)"
status="$(request GET "/v1/users/$current_user_id" "" "$token")"
assert_status "Fetch missing user" "$status" "404"

echo "Scenario: Update non-existent user (after deletion)"
status="$(request PATCH "/v1/users/$current_user_id" '{"name":"Nope"}' "$token")"
assert_status "Update missing user" "$status" "404"

if [[ "$failures" -gt 0 ]]; then
  echo "Scenario run completed with $failures failures"
  exit 1
fi

echo "All scenarios passed"
