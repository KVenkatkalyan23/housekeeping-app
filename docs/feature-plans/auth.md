# Feature: JWT Authentication

## Goal

Implement role-based JWT authentication across backend and frontend with two roles: `ADMIN` and `STAFF`.

---

## Scope

- login API
- add user API
- JWT generation
- JWT validation filter
- Spring Security configuration
- role-based authorization
- password hashing
- user loading from database
- authentication and authorization error handling
- frontend login page
- frontend auth state management
- token storage
- API integration
- protected routes
- logout flow
- role-based route handling

---

## Out of Scope

- signup
- refresh token
- forgot password
- email verification
- profile management

---

## Files (MANDATORY)

### Backend

#### auth/controller/

- `AuthController.java`

#### auth/service/

- `AuthService.java`

#### auth/dto/

- `LoginRequest.java`
- `LoginResponse.java`
- `CreateUserRequest.java`
- `CreateUserResponse.java`

#### auth/security/

- `JwtService.java`
- `JwtAuthenticationFilter.java`
- `SecurityConfig.java`
- `CustomUserDetailsService.java`

#### auth/entity/

- `User.java` (reuse existing if already created)
- `Role.java` (reuse existing if already created)

#### auth/repository/

- `UserRepository.java` (reuse existing or update)

---

### Frontend

#### features/auth/

- `api.ts`
- `slice.ts`
- `types.ts`
- `pages/LoginPage.tsx`
- `components/ProtectedRoute.tsx`

#### shared/api/

- `client.ts`

#### routes/

- update route definitions to protect authenticated pages
- add role-based route handling if route split already exists

#### store/

- update root store if needed to register auth slice

---

## General Rules

- Use Spring Security on backend
- Use JWT for stateless authentication
- Roles must be `ADMIN` and `STAFF`
- Default role = `STAFF`
- Keep implementation minimal and clean
- Reuse existing `User` entity and `Role` enum if already present
- Do not create unnecessary abstractions
- Frontend must not hardcode auth logic outside auth module and route protection

---

## Backend API Endpoints

### POST `/api/auth/login`

Purpose:

- authenticate user using username and password
- return JWT token and basic user info

Request:

- username
- password

Response:

- accessToken
- tokenType
- userId
- username
- role

### POST `/api/auth/users`

Purpose:

- create user
- only ADMIN allowed

Request:

- username
- password
- role (optional, default = `STAFF`)

Response:

- userId
- username
- role

---

## Security Rules

### Public Endpoints

- `/api/auth/login`

### Protected Endpoints

- `/api/auth/users`
- all other non-public APIs

### Role-based Access

- `ADMIN` can create users
- `ADMIN` can access admin APIs
- `STAFF` can access staff APIs
- shared authenticated APIs can be accessed by both

---

## JWT Requirements

- include:
  - `userId`
  - `username`
  - `role`
- extract from:
  - `Authorization: Bearer <token>`
- validate on every request
- use secure signing key from configuration
- define token expiration in configuration

---

## Frontend Authentication Flow

### Login

- login form submits username and password to `/api/auth/login`
- on success:
  - store token
  - store basic user data from response
  - redirect based on role or app route rules

### Token Storage

- store JWT in localStorage
- hydrate auth state from localStorage on app load

### API Integration

- use centralized API client
- attach `Authorization: Bearer <token>` header automatically for authenticated requests
- handle 401 responses by clearing auth state and redirecting to login

### Protected Routes

- block unauthenticated users from protected pages
- redirect unauthenticated users to `/login`
- support role-based restrictions where needed

### Logout

- clear token from localStorage
- clear auth state
- redirect to login

---

## Frontend State

### Auth Slice

State should include:

- accessToken
- userId
- username
- role
- isAuthenticated

Actions should include:

- loginSuccess
- logout
- hydrateFromStorage

---

## Password Handling

- use `BCryptPasswordEncoder`
- passwords must never be stored or compared in plain text

---

## User Loading

- load user from database using username
- map role to Spring Security authority correctly
- convert role to Spring Security format if needed:
  - `ADMIN` → `ROLE_ADMIN`
  - `STAFF` → `ROLE_STAFF`

---

## Validation

### Backend

#### LoginRequest

- `@NotBlank` username
- `@NotBlank` password

#### CreateUserRequest

- `@NotBlank` username
- `@NotBlank` password
- optional role
- if role is missing, assign `STAFF`

### Frontend

- validate required username
- validate required password
- show API error message for invalid credentials

---

## Configuration

Add backend configuration for:

- JWT secret
- JWT expiration time

---

## Done Criteria

- login API works
- create user API works
- JWT is issued correctly
- protected backend APIs are secured
- role-based backend access works
- passwords are hashed
- token contains required fields
- frontend login is integrated with backend
- token is persisted and restored
- protected routes work
- logout works
- auth flow is clean and minimal
