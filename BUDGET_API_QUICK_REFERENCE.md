# Budget API - Quick Reference Guide

## REST Endpoints Summary

### Base URL
```
http://localhost:8080/api/DB/budget
```

### 1. Test Service
```
GET /test
Response: "Budget service is up and running!"
```

### 2. Create Budget
```
POST /create/{userId}

Path Parameter:
  userId (required): String - User identifier

Body:
{
  "categoryId": 1,              // required: Long
  "year": 2026,                 // required: Integer
  "month": 6,                   // required: Integer (1-12)
  "amount": 5000.00,            // required: BigDecimal
  "isActive": true,             // optional: Boolean (default: true)
  "recurrence": "monthly"       // optional: String (default: "monthly")
}

Response: Budget object with generated id, createdAt, updatedAt
```

### 3. Get Budget by ID
```
GET /{budgetId}/{userId}

Path Parameters:
  budgetId (required): Long - Budget identifier
  userId (required): String - User identifier

Response: Budget object (only if belongs to user)
```

### 4. Get All Budgets for User
```
GET /user/{userId}

Path Parameter:
  userId (required): String - User identifier

Response: List of Budget objects
```

### 5. Get Budgets by Date Range
```
GET /user/{userId}/range?startYear=2026&startMonth=1&endYear=2026&endMonth=12

Path Parameter:
  userId (required): String - User identifier

Query Parameters:
  startYear (required): Integer - Start year
  startMonth (required): Integer - Start month (1-12)
  endYear (required): Integer - End year
  endMonth (required): Integer - End month (1-12)

Response: List of Budget objects within date range
```

### 6. Update Budget
```
PUT /{budgetId}/{userId}

Path Parameters:
  budgetId (required): Long - Budget identifier
  userId (required): String - User identifier

Body (all optional):
{
  "amount": 5500.00,
  "categoryId": 1,
  "year": 2026,
  "month": 7,
  "isActive": true,
  "recurrence": "monthly"
}

Response: Updated Budget object
```

### 7. Delete Budget
```
DELETE /{budgetId}/{userId}

Path Parameters:
  budgetId (required): Long - Budget identifier
  userId (required): String - User identifier

Response: true (Boolean)
```

---

## Error Responses

All errors follow the same format (caught by GlobalExceptionHandler):

```json
{
  "error": true,
  "message": "Error description",
  "errorDetails": {
    "errorCode": "ERROR_CODE",
    "timestamp": "2026-06-02T15:25:00"
  }
}
```

### Common Error Codes

| Code | Message | Cause |
|------|---------|-------|
| INVALID_INPUT | User ID cannot be null or empty | userId parameter missing or empty |
| INVALID_INPUT | Category ID cannot be null | categoryId field missing |
| INVALID_INPUT | Year cannot be null | year field missing |
| INVALID_INPUT | Month must be between 1 and 12 | Invalid month value |
| INVALID_INPUT | Amount cannot be null | amount field missing |
| RESOURCE_ALREADY_EXISTS | Budget already exists for this user/category/period | Duplicate budget |
| INVALID_REFERENCE | Budget not found | Budget ID doesn't exist |
| INVALID_REFERENCE | Budget does not belong to this user | User trying to access another's budget |

---

## GraphQL Queries and Mutations

### Endpoint
```
POST http://localhost:8080/graphql
Content-Type: application/json
```

### Query: Get Budget by ID
```graphql
{
  getBudgetById(id: 1, userId: "user123") {
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

### Query: Get All Budgets for User
```graphql
{
  getBudgetsByUserId(userId: "user123") {
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

### Query: Get Budgets by Date Range
```graphql
{
  getBudgetsByUserIdAndDateRange(input: {
    userId: "user123"
    startYear: 2026
    startMonth: 1
    endYear: 2026
    endMonth: 12
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

### Mutation: Create Budget
```graphql
mutation {
  createBudget(input: {
    userId: "user123"
    categoryId: 1
    year: 2026
    month: 6
    amount: 5000.00
    isActive: true
    recurrence: "monthly"
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

### Mutation: Update Budget
```graphql
mutation {
  updateBudget(
    id: 1
    userId: "user123"
    input: {
      userId: "user123"
      categoryId: 1
      year: 2026
      month: 7
      amount: 5500.00
    }
  ) {
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

### Mutation: Delete Budget
```graphql
mutation {
  deleteBudget(id: 1, userId: "user123")
}
```

---

## Testing Workflow

### 1. Health Check
```bash
curl http://localhost:8080/api/DB/budget/test
```

### 2. Create Budget
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
Returns: `{"id": 1, "userId": "user123", ...}`

### 3. Get All Budgets
```bash
curl http://localhost:8080/api/DB/budget/user/user123
```

### 4. Get Specific Budget
```bash
curl http://localhost:8080/api/DB/budget/1/user123
```

### 5. Update Budget
```bash
curl -X PUT http://localhost:8080/api/DB/budget/1/user123 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5500.00}'
```

### 6. Delete Budget
```bash
curl -X DELETE http://localhost:8080/api/DB/budget/1/user123
```

---

## Key Security Features

✅ **User ID Validation**: All operations require userId
✅ **User Isolation**: Cannot access other users' budgets
✅ **Input Validation**: Comprehensive validation
✅ **Duplicate Prevention**: Same user can't create duplicate budgets
✅ **Error Handling**: No sensitive data exposed

---

## Database Schema

```sql
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    category_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    recurrence VARCHAR(20) DEFAULT 'monthly',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT uq_budget UNIQUE (user_id, category_id, year, month),
    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id) REFERENCES userDetails(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);
```

---

## Files Modified/Created

### New Files:
1. `src/main/java/com/expenses_traker/db_service/pojo/BudgetPojo.java`
2. `src/main/java/com/expenses_traker/db_service/Controller/BudgetController.java`
3. `postman/collections/budget-crud.postman_collection.json`
4. `BUDGET_API_DOCUMENTATION.md`
5. `BUDGET_API_QUICK_REFERENCE.md` (this file)

### Modified Files:
1. `src/main/java/com/expenses_traker/db_service/Service/BudgetService.java`
2. `src/main/java/com/expenses_traker/db_service/GraphQLReslover/BudgetGraphQLController.java`
3. `src/main/resources/graphql/Budget.graphqls`

---

## Notes

- All endpoints are reactive (return Mono/Flux)
- Error handling is done via AppException
- Timestamps are automatically managed (createdAt, updatedAt)
- Default values: `isActive = true`, `recurrence = "monthly"`
- Month must be between 1 and 12
- Consider userId as the tenant identifier for multi-tenancy

