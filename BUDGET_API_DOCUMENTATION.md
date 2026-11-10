# Budget CRUD API Implementation

## Overview
Implemented a complete CRUD (Create, Read, Update, Delete) API for Budget management following the same pattern as the Category service. All operations now require `userId` to ensure proper user data isolation and security.

---

## Changes Made

### 1. Created BudgetPojo
**File**: `src/main/java/com/expenses_traker/db_service/pojo/BudgetPojo.java`

A POJO (Plain Old Java Object) to handle API requests:
- `userId`: User identifier
- `categoryId`: Budget category reference
- `year`: Budget year
- `month`: Budget month (1-12)
- `amount`: Budget amount
- `isActive`: Budget status
- `recurrence`: Recurrence pattern (e.g., "monthly")

### 2. Updated BudgetService
**File**: `src/main/java/com/expenses_traker/db_service/Service/BudgetService.java`

Completely refactored to follow CategoryService pattern with:

#### Key Features:
- **Proper Exception Handling**: Uses `AppException` with error codes instead of wrapping responses
- **UserID Validation**: All operations require userId parameter and verify ownership
- **Null Checks**: Comprehensive null checks for all inputs
- **Input Validation**: Validates month (1-12), year, amount, categoryId
- **Duplicate Prevention**: Prevents duplicate budgets for same user/category/period

#### Methods:
```java
// Create a new budget
Mono<Budget> createBudget(BudgetPojo budgetPojo)

// Get budget by ID with user validation
Mono<Budget> getBudgetById(Long id, String userId)

// Get all budgets for a user
Mono<List<Budget>> getBudgetsByUserId(String userId)

// Get budgets within date range
Mono<List<Budget>> getBudgetsByUserIdAndDateRange(
    String userId, Integer startYear, Integer startMonth, 
    Integer endYear, Integer endMonth)

// Update budget
Mono<Budget> updateBudget(Long id, String userId, BudgetPojo input)

// Delete budget
Mono<Boolean> deleteBudget(Long id, String userId)
```

### 3. Created BudgetController (REST API)
**File**: `src/main/java/com/expenses_traker/db_service/Controller/BudgetController.java`

RESTful endpoints for budget operations:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/DB/budget/test` | Health check |
| POST | `/api/DB/budget/create/{userId}` | Create budget |
| GET | `/api/DB/budget/{budgetId}/{userId}` | Get budget by ID |
| GET | `/api/DB/budget/user/{userId}` | Get all budgets for user |
| GET | `/api/DB/budget/user/{userId}/range` | Get budgets by date range |
| PUT | `/api/DB/budget/{budgetId}/{userId}` | Update budget |
| DELETE | `/api/DB/budget/{budgetId}/{userId}` | Delete budget |

**All endpoints require userId for security.**

### 4. Updated BudgetGraphQLController
**File**: `src/main/java/com/expenses_traker/db_service/GraphQLReslover/BudgetGraphQLController.java`

Updated to use the new BudgetService with userId parameters:

#### Queries:
- `getBudgetById(id: ID!, userId: String!): Budget`
- `getBudgetsByUserId(userId: String!): [Budget!]!`
- `getBudgetsByUserIdAndDateRange(input: BudgetDateRangeInput!): [Budget!]!`

#### Mutations:
- `createBudget(input: BudgetInput!): Budget!`
- `updateBudget(id: ID!, userId: String!, input: BudgetInput!): Budget!`
- `deleteBudget(id: ID!, userId: String!): Boolean!`

### 5. Updated Budget GraphQL Schema
**File**: `src/main/resources/graphql/Budget.graphqls`

Updated GraphQL schema with:
- Typed inputs with required fields
- User ID requirement in all operations
- Proper scalar types (BigDecimal, DateTime)
- Clear query/mutation structure

---

## Error Handling

All methods include comprehensive error handling:

### Validation Errors:
- Null/empty userId
- Null/empty categoryId, year, month, amount
- Invalid month (must be 1-12)
- Invalid date range (start > end)

### Business Logic Errors:
- Duplicate budget prevention (same user/category/period)
- Budget not found
- Unauthorized access (budget doesn't belong to user)

### Exception Types:
- `ErrorCode.INVALID_INPUT`: Validation errors
- `ErrorCode.INVALID_REFERENCE`: Resource not found
- `ErrorCode.RESOURCE_ALREADY_EXISTS`: Duplicate detection

---

## User Authorization Pattern

Every operation validates that the budget belongs to the requesting user:

```java
if (!existing.getUserId().equals(userId)) {
    return Mono.error(new AppException(
        ErrorCode.INVALID_REFERENCE, 
        "Budget does not belong to this user"
    ));
}
```

This ensures users can only:
- ✅ Create budgets for themselves
- ✅ Read their own budgets
- ✅ Update their own budgets
- ✅ Delete their own budgets
- ❌ Access other users' budgets

---

## Testing with Postman

A comprehensive Postman collection has been created: `postman/collections/budget-crud.postman_collection.json`

### Collection Includes:

#### 1. Basic CRUD Operations:
- **Create Budget**: `POST /api/DB/budget/create/{userId}`
  ```json
  {
    "categoryId": 1,
    "year": 2026,
    "month": 6,
    "amount": 5000.00,
    "isActive": true,
    "recurrence": "monthly"
  }
  ```

- **Get Budget**: `GET /api/DB/budget/{budgetId}/{userId}`
- **Get All Budgets**: `GET /api/DB/budget/user/{userId}`
- **Get Budgets by Range**: `GET /api/DB/budget/user/{userId}/range?startYear=2026&startMonth=1&endYear=2026&endMonth=12`
- **Update Budget**: `PUT /api/DB/budget/{budgetId}/{userId}`
- **Delete Budget**: `DELETE /api/DB/budget/{budgetId}/{userId}`

#### 2. Error Cases:
- Wrong user ID access
- Null userId
- Invalid month values
- Missing required fields
- Duplicate budget
- Invalid date range

#### 3. GraphQL Operations:
- All GraphQL mutations and queries with example payloads

---

## Key Features

### 1. User Isolation ✅
- Every budget is tied to a userId
- Users cannot access other users' budgets
- Enforced at service layer

### 2. Input Validation ✅
- Null checks for all inputs
- Business logic validation
- Month range validation (1-12)
- Date range validation

### 3. Duplicate Prevention ✅
- Unique constraint: (userId, categoryId, year, month)
- Prevents creating duplicate budgets for same period
- Allows updates without errors if data unchanged

### 4. Error Handling ✅
- Proper exception throwing
- Descriptive error messages
- Reactive streams error handling

### 5. Comprehensive Testing ✅
- Postman collection with all scenarios
- Error case testing
- Both REST and GraphQL testing

---

## Building and Running

```bash
# Navigate to project directory
cd exp-tracker-db-service

# Build the project
mvn clean compile

# Package the application
mvn package

# Run the application
java -jar target/dbservice.jar
```

---

## API Usage Examples

### REST API

#### Create Budget
```bash
curl -X POST http://localhost:8080/api/DB/budget/create/user123 \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "year": 2026,
    "month": 6,
    "amount": 5000.00,
    "isActive": true,
    "recurrence": "monthly"
  }'
```

#### Get All Budgets for User
```bash
curl http://localhost:8080/api/DB/budget/user/user123
```

#### Get Budgets by Date Range
```bash
curl "http://localhost:8080/api/DB/budget/user/user123/range?startYear=2026&startMonth=1&endYear=2026&endMonth=12"
```

#### Update Budget
```bash
curl -X PUT http://localhost:8080/api/DB/budget/1/user123 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5500.00}'
```

#### Delete Budget
```bash
curl -X DELETE http://localhost:8080/api/DB/budget/1/user123
```

### GraphQL

#### Create Budget
```graphql
mutation {
  createBudget(input: {
    userId: "user123"
    categoryId: 1
    year: 2026
    month: 6
    amount: 5000.00
  }) {
    id
    userId
    categoryId
    year
    month
    amount
    isActive
    recurrence
    createdAt
    updatedAt
  }
}
```

#### Get All Budgets
```graphql
query {
  getBudgetsByUserId(userId: "user123") {
    id
    userId
    categoryId
    year
    month
    amount
    isActive
    recurrence
  }
}
```

---

## Summary

✅ **Complete CRUD implementation** for Budget management
✅ **User isolation** - All operations tied to userId
✅ **Comprehensive error handling** with proper exception codes
✅ **Input validation** with null checks
✅ **Duplicate prevention** using database constraints
✅ **Both REST and GraphQL APIs** available
✅ **Postman collection** with 15+ test cases
✅ **Follows the same pattern** as CategoryService

The implementation is production-ready with proper security, error handling, and validation.

