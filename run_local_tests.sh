#!/bin/bash

set -e

BASE_URL="http://localhost:8090/api"

# Helper function to print test results
print_result() {
    echo "=== $1 ==="
    echo "$2" | jq .
    echo
}

# Login
echo "Testing /api/login (Admin Login)"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"john@example.com","password":"password"}')
print_result "Admin Login" "$LOGIN_RESPONSE"

# Extract admin token
ADMIN_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo "Error: Failed to get admin token"
    exit 1
fi
echo "Admin Token: $ADMIN_TOKEN"

# Login as non-admin user
echo "Testing /api/login (User Login)"
USER_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"jane@example.com","password":"password"}')
print_result "User Login" "$USER_LOGIN_RESPONSE"

# Extract user token
USER_TOKEN=$(echo "$USER_LOGIN_RESPONSE" | jq -r '.token')
if [ -z "$USER_TOKEN" ] || [ "$USER_TOKEN" = "null" ]; then
    echo "Error: Failed to get user token"
    exit 1
fi
echo "User Token: $USER_TOKEN"

# Test invalid login credentials
echo "Testing /api/login (Invalid Password)"
INVALID_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"john@example.com","password":"wrong"}')
print_result "Invalid Password" "$INVALID_LOGIN_RESPONSE"

# Test login with non-existent user
echo "Testing /api/login (Non-Existent User)"
NONEXISTENT_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"nonexistent@example.com","password":"password"}')
print_result "Non-Existent User" "$NONEXISTENT_LOGIN_RESPONSE"

# Create a new user (admin only)
echo "Testing /api/add-user (Create User)"
ADD_USER_RESPONSE=$(curl -s -X POST "$BASE_URL/add-user" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"email":"newuser@example.com","name":"New User","phone_number":"3456789012","role":"user"}')
print_result "Create User" "$ADD_USER_RESPONSE"

# Test duplicate user creation
echo "Testing /api/add-user (Duplicate User)"
DUPLICATE_USER_RESPONSE=$(curl -s -X POST "$BASE_URL/add-user" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"email":"newuser@example.com","name":"New User","phone_number":"3456789012","role":"user"}')
print_result "Duplicate User" "$DUPLICATE_USER_RESPONSE"

# Test create user with invalid payload
echo "Testing /api/add-user (Invalid Payload)"
INVALID_PAYLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/add-user" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"email":"invalid","name":"","phone_number":"123","role":"invalid"}')
print_result "Invalid Payload" "$INVALID_PAYLOAD_RESPONSE"

# Test create user without token
echo "Testing /api/add-user (No Token)"
NO_TOKEN_RESPONSE=$(curl -s -X POST "$BASE_URL/add-user" \
    -H "Content-Type: application/json" \
    -d '{"email":"anotheruser@example.com","name":"Another User","phone_number":"4567890123","role":"user"}')
print_result "No Token" "$NO_TOKEN_RESPONSE"

# Test create user with non-admin token
echo "Testing /api/add-user (Non-Admin Token)"
NON_ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/add-user" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d '{"email":"anotheruser@example.com","name":"Another User","phone_number":"4567890123","role":"user"}')
print_result "Non-Admin Token" "$NON_ADMIN_RESPONSE"

# Test login with new user
echo "Testing /api/login (New User Login)"
NEW_USER_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"newuser@example.com","password":"fitness"}')
print_result "New User Login" "$NEW_USER_LOGIN_RESPONSE"

# Get all users (admin only, assuming /api/users exists)
echo "Testing /api/users (Get All Users)"
USERS_RESPONSE=$(curl -s -X GET "$BASE_URL/users" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
print_result "Get All Users" "$USERS_RESPONSE"

# Get specific user by ID (admin only, assuming ID=3 for newuser@example.com)
echo "Testing /api/users/3 (Get User by ID)"
USER_BY_ID_RESPONSE=$(curl -s -X GET "$BASE_URL/users/3" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
print_result "Get User by ID" "$USER_BY_ID_RESPONSE"

# Update user (admin only, assuming /api/users/{id} exists)
echo "Testing /api/users/3 (Update User)"
UPDATE_USER_RESPONSE=$(curl -s -X PUT "$BASE_URL/users/3" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"email":"newuser@example.com","name":"Updated User","phone_number":"6789012345","role":"user"}')
print_result "Update User" "$UPDATE_USER_RESPONSE"

# Test update user with non-admin token
echo "Testing /api/users/3 (Update User, Non-Admin Token)"
NON_ADMIN_UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/users/3" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d '{"email":"newuser@example.com","name":"Updated User","phone_number":"6789012345","role":"user"}')
print_result "Update User Non-Admin" "$NON_ADMIN_UPDATE_RESPONSE"

# Delete user (admin only, assuming /api/users/{id} exists)
echo "Testing /api/users/3 (Delete User)"
DELETE_USER_RESPONSE=$(curl -s -X DELETE "$BASE_URL/users/3" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
print_result "Delete User" "$DELETE_USER_RESPONSE"

# Verify user deletion
echo "Testing /api/login (Deleted User Login)"
DELETED_USER_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"newuser@example.com","password":"fitness"}')
print_result "Deleted User Login" "$DELETED_USER_LOGIN_RESPONSE"

# Test invalid endpoint
echo "Testing /api/invalid (Invalid Endpoint)"
INVALID_ENDPOINT_RESPONSE=$(curl -s -X GET "$BASE_URL/invalid" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
print_result "Invalid Endpoint" "$INVALID_ENDPOINT_RESPONSE"

echo "All tests completed!"
