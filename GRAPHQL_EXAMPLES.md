# GraphQL Budget API - Query Examples

## Controller Pattern (Like CategoryGraphQLController)

The BudgetGraphQLController now follows the same pattern as CategoryGraphQLController using `@Argument` annotations for simple query parameters instead of complex input types.

---

## GraphQL Queries

### 1. Get Budget by ID
```graphql
query {
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

### 2. Get All Budgets for User
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
    createdAt
    updatedAt
  }
}
```

### 3. Get Budgets by Date Range
```graphql
query {
  getBudgetsByUserIdAndDateRange(
    userId: "user123"
    startYear: 2026
    startMonth: 1
    endYear: 2026
    endMonth: 12
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

---

## GraphQL Mutations

### 1. Create Budget
```graphql
mutation {
  createBudget(
    userId: "user123"
    categoryId: 1
    year: 2026
    month: 6
    amount: 5000.00
    isActive: true
    recurrence: "monthly"
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

**Minimal Create (using defaults):**
```graphql
mutation {
  createBudget(
    userId: "user123"
    categoryId: 1
    year: 2026
    month: 6
    amount: 5000.00
  ) {
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

### 2. Update Budget
```graphql
mutation {
  updateBudget(
    id: 1
    userId: "user123"
    year: 2026
    month: 7
    amount: 5500.00
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

**Update with Multiple Fields:**
```graphql
mutation {
  updateBudget(
    id: 1
    userId: "user123"
    categoryId: 2
    year: 2026
    month: 7
    amount: 5500.00
    isActive: true
    recurrence: "monthly"
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

### 3. Delete Budget
```graphql
mutation {
  deleteBudget(id: 1, userId: "user123")
}
```

---

## Testing in GraphQL IDE

1. Go to `http://localhost:8080/graphql`
2. Paste any query or mutation above
3. Click Play button
4. View results in right panel

---

## Required vs Optional Parameters

### Queries - Required Parameters:
- `getBudgetById`: `id` ✅, `userId` ✅
- `getBudgetsByUserId`: `userId` ✅
- `getBudgetsByUserIdAndDateRange`: `userId` ✅, `startYear` ✅, `startMonth` ✅, `endYear` ✅, `endMonth` ✅

### Mutations - Create Budget:
- **Required**: `userId`, `categoryId`, `year`, `month`, `amount`
- **Optional**: `isActive` (defaults to true), `recurrence` (defaults to "monthly")

### Mutations - Update Budget:
- **Required**: `id`, `userId`
- **Optional**: All other fields (`categoryId`, `year`, `month`, `amount`, `isActive`, `recurrence`)

### Mutations - Delete Budget:
- **Required**: `id`, `userId`

---

## Key Differences from REST API

| Aspect | REST API | GraphQL API |
|--------|----------|-------------|
| Method | POST, GET, PUT, DELETE | POST (all) |
| Endpoint | `/api/DB/budget/...` | `/graphql` |
| Parameters | Path + Query params | Query arguments |
| Response | JSON object | JSON object |
| Error Handling | HTTP Status codes | GraphQL `errors` field |

---

## Sample Responses

### Success Response - Get Budget
```json
{
  "data": {
    "getBudgetById": {
      "id": "1",
      "userId": "user123",
      "categoryId": 1,
      "year": 2026,
      "month": 6,
      "amount": 5000.00,
      "isActive": true,
      "recurrence": "monthly",
      "createdAt": "2026-06-02T15:24:54Z",
      "updatedAt": "2026-06-02T15:24:54Z"
    }
  }
}
```

### Error Response - Invalid User
```json
{
  "errors": [
    {
      "message": "Budget does not belong to this user",
      "locations": [
        {
          "line": 2,
          "column": 3
        }
      ],
      "path": [
        "getBudgetById"
      ]
    }
  ],
  "data": {
    "getBudgetById": null
  }
}
```

---

## Pattern Alignment with CategoryGraphQLController

✅ Uses `@QueryMapping` for read operations
✅ Uses `@MutationMapping` for write operations
✅ Uses `@Argument` annotation for parameters
✅ No complex input types (BudgetInput removed)
✅ Simple parameter passing
✅ Returns entity directly
✅ Error handling via Spring GraphQL

---

## Implementation Details

The GraphQL controller implementation:

```java
@QueryMapping
public Mono<Budget> getBudgetById(@Argument Long id, @Argument String userId) {
    return budgetService.getBudgetById(id, userId);
}

@MutationMapping
public Mono<Budget> createBudget(
        @Argument String userId,
        @Argument Long categoryId,
        @Argument Integer year,
        @Argument Integer month,
        @Argument BigDecimal amount,
        @Argument Boolean isActive,
        @Argument String recurrence) {
    // ... logic to create BudgetPojo and call service
    return budgetService.createBudget(pojo);
}
```

This matches the CategoryGraphQLController pattern exactly!

---

## Tips

1. **Null Handling**: Optional parameters can be omitted and will be null
2. **Type Safety**: GraphQL automatically validates argument types
3. **Documentation**: Use GraphQL introspection to see all available fields
4. **Testing**: Postman now includes GraphQL examples in the collection
5. **Debugging**: GraphQL provides detailed error messages with field paths

---

Generated on: June 2, 2026
Pattern: CategoryGraphQLController style with @Argument annotations

