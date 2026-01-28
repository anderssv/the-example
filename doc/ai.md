# AI-Friendly TDD Techniques Summary

This document provides a structured summary of Test-Driven Development techniques for AI agents working on this codebase.

## Core Philosophy

Write tests first (TDD approach). Tests should be:
- Predictable (not flaky)
- Readable
- Easy to write
- Maintainable (resistant to irrelevant changes)
- Fast

## Three Main Techniques

### 1. Test Setup (Test Data & System Configuration)

**Purpose**: Centralize and reuse test data and system setup to reduce coupling and improve maintainability.

**Implementation**:
- Use extension methods on companion objects to create valid test data
- Follow naming convention: `ClassName.valid()` with optional parameters for variations
- Use Kotlin's `copy()` method for simple variations
- Store these in `TestExtensions.kt` or similar

**Example**:
```kotlin
fun Application.Companion.valid(addToMonth: Long = 0) = Application(
    id = UUID.randomUUID(),
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = LocalDate.of(2022, 2, 15).plusMonths(addToMonth),
    status = ApplicationStatus.ACTIVE
)

// Usage in tests:
val application = Application.valid()
val oldApplication = Application.valid(addToMonth = -7)
val deniedApplication = Application.valid().copy(status = DENIED)
```

**System Context Setup**:
- Create a `SystemTestContext` that extends production `SystemContext`
- Override repositories and clients with Fakes
- Use `with(testContext) { ... }` for easy access to services and repositories

**Benefits**:
- Tests don't break when unrelated domain fields change
- Easy to find and reuse test data patterns
- Single location for test data setup

### 2. Fakes (Test Doubles)

**Purpose**: Replace external dependencies (DB, HTTP clients, etc.) with in-memory implementations for fast, reliable, reusable tests.

**Key Principle**: Interface should work with domain objects, NOT DTOs. Keep DTOs internal to the implementation.

**Implementation**:
```kotlin
class CustomerRepositoryFake : CustomerRepository {
    private val db = mutableMapOf<String, Customer>()

    override fun addCustomer(customer: Customer) {
        db[customer.name] = customer
    }

    override fun getCustomer(name: String): Customer {
        return db[name]!!
    }
    
    // Custom methods for testing (not in interface)
    fun registerFailure(id: String) { /* ... */ }
}
```

**Characteristics**:
- HashMap-based implementation (most common)
- Implement interface methods incrementally as tests need them
- Add custom methods for error injection and verification (outside the interface)
- Work with domain objects, not DTOs

**When to Use Fakes**:
- Default choice for all external dependencies
- DB repositories
- External API clients
- Notification services

**Benefits**:
- Blazing fast (all in-memory)
- No flakiness
- Reusable across all tests
- No mock framework needed
- Tests survive behavior changes better
- Changes in data structures require fewer test updates

**Error Testing**:
```kotlin
// In Fake:
private val failures = mutableSetOf<String>()
fun failOnOrgNumber(orgNumber: String) { failures.add(orgNumber) }

override fun getCompany(orgNumber: String): Company {
    if (failures.contains(orgNumber)) throw CompanyNotFoundException()
    return db[orgNumber]!!
}
```

**Verification**:
- Prefer verifying system state (outcomes) over interactions
- Add custom methods to Fakes when interaction verification is truly needed

### 3. Testing Through The Domain (TTTD)

**Purpose**: Use domain methods to set up test state instead of directly manipulating storage. This reduces test brittleness and improves domain design.

**Two Approaches**:

**Data-Oriented (Avoid)**:
```kotlin
@Test
fun testApproval() {
    val application = Application.valid()
    applicationRepo.addApplication(application)  // Direct storage manipulation
    
    appService.approveApplication(application.id)
    
    assertThat(applicationRepo.getApplication(application.id).status)
        .isEqualTo(APPROVED)
}
```

**Domain-Oriented (Prefer)**:
```kotlin
@Test
fun testApproval() {
    val application = Application.valid()
    appService.registerInitialApplication(application)  // Domain method
    
    appService.approveApplication(application.id)
    
    assertThat(applicationRepo.getApplication(application.id).status)
        .isEqualTo(APPROVED)
}
```

**Why This Matters**:
- When requirements change (e.g., "approval needs to check customer status"), data-oriented tests fail
- Domain-oriented tests survive because `registerInitialApplication()` maintains all invariants
- Forces you to create clear, reusable domain operations
- Tests become documentation of valid state transitions

**Benefits**:
- Tests survive refactoring
- Fewer false failures when logic changes
- Domain code becomes clearer and more expressive
- Reduces coupling between tests and system behavior

## Test Types & When to Use Each

### Domain Tests (No Fakes)
- Pure business logic
- No I/O, no infrastructure
- Fast, focused tests

### IO Tests (No Fakes)
- Testing SQL queries in repositories
- Testing HTTP protocol details in clients
- Ensures adapters work correctly with real systems

### Variation Tests (With Fakes)
- Focus on specific system behavior variations
- Example: "Email should include rejection reason when application is denied"
- Tests edge cases and conditional logic

### Outcome Tests (With Fakes)
- Full feature flows through multiple components
- Example: "Email sent with rejection subject when application is rejected"
- Tests integration between components

## Test Structure Pattern

Follow Arrange-Act-Assert:

```kotlin
@Test
fun testFeature() {
    with(testContext) {
        // Arrange - Set up through domain methods
        val application = Application.valid()
        applicationService.registerInitialApplication(application)
        
        // Act - Perform the action being tested
        applicationService.approveApplication(application.id)
        
        // Assert - Verify outcomes (state, not interactions)
        assertThat(repositories.applicationRepo.getApplication(application.id).status)
            .isEqualTo(APPROVED)
    }
}
```

## Manual Dependency Injection

**Why Manual DI**:
- Full control over what's loaded and when
- Easy to inject Fakes for testing
- No framework annotations to learn
- Better feedback on code structure
- Faster test startup (only load what's needed)

**Structure**:
```kotlin
// Production context
open class SystemContext {
    open class Repositories {
        open val applicationRepo: ApplicationRepository = ApplicationRepositoryImpl()
    }
    
    open val repositories = Repositories()
    open val applicationService by lazy { ApplicationService(repositories.applicationRepo) }
}

// Test context
class SystemTestContext : SystemContext() {
    class Repositories : SystemContext.Repositories() {
        override val applicationRepo = ApplicationRepositoryFake()
    }
    
    override val repositories = Repositories()
}
```

## Additional Patterns

### Sum Types for Validation
- Parse and validate at system edges (controllers, API boundaries)
- Use sealed classes for representing states
- Operate on strongly typed objects internally
- Follow "parse, don't validate" principle

### Time Handling in Tests
- Create test DSLs for complex time-based scenarios
- Inject clock dependencies for deterministic time testing

### Naming Conventions
- `*Domain.kt` - Domain models
- `*Repository.kt` - Data access interfaces
- `*RepositoryFake.kt` - Test doubles for repositories
- `*Client.kt` - External service clients
- `*ClientFake.kt` - Test doubles for clients
- `*Service.kt` - Business logic services

## Quick Decision Guide for AI Agents

**When writing a test**:
1. Need test data? → Use `ClassName.valid()` or `.valid().copy(...)`
2. Need system setup? → Use `SystemTestContext()` or `with(testContext)`
3. Need external dependency? → Check for existing Fake, or create HashMap-based Fake
4. Setting up state? → Use service/domain methods, NOT direct repository calls
5. Verifying results? → Check state/outcomes, NOT interaction counts

**When creating a Fake**:
1. Implement the interface
2. Use `mutableMapOf<KeyType, DomainObject>()` for storage
3. Implement methods incrementally (IntelliJ "implement interface" → TODOs)
4. Add custom error injection methods as needed
5. Interface works with domain objects; DTOs stay inside the implementation

**When adding domain logic**:
1. Write test first using domain-oriented setup
2. Create/use service methods for state mutation
3. Verify outcomes through queries or repository state
4. Let test needs drive domain method creation

## Common Mistakes to Avoid

1. Using DTOs at interface boundaries → Keep DTOs internal, use domain objects
2. Direct repository manipulation in tests → Use service methods instead
3. Verifying interactions instead of outcomes → Check state changes
4. Complex Fake implementations → Start with HashMap, add complexity only when needed
5. Mocking everything → Default to Fakes, use mocks rarely (HTTP protocol testing)
6. Not reusing test data setup → Create `.valid()` extensions early

## Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "fully.qualified.TestClassName"

# Compile only (faster feedback during TDD)
./gradlew compileTestKotlin
```

## Key Files to Reference

- Example test with all techniques: `src/test/kotlin/application/ApplicationFakeTest.kt`
- Test extensions: `src/test/kotlin/application/TestExtensions.kt`
- Fake examples: `src/test/kotlin/application/ApplicationRepositoryFake.kt`
- System context: `src/main/kotlin/system/SystemContext.kt`
- Test context: `src/test/kotlin/system/SystemTestContext.kt`
- TTTD example: `src/test/kotlin/application/TestingThroughTheDomainTest.kt`

## Related Documentation

- [TDD Overview](tdd.md) - Full philosophy and approach
- [Fakes](fakes.md) - Detailed guide on creating and using fakes
- [Testing Through the Domain](tttd.md) - Deep dive on domain-oriented testing
- [Test Setup](test-setup.md) - Comprehensive test data patterns
- [Manual Dependency Injection](manual-dependency-injection.md) - DI approach
- [TDD Concepts Overview](tdd-concepts-overview.md) - Visual relationship diagrams
