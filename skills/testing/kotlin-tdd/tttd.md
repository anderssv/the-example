# Testing Through The Domain (TTTD)

Set up test state using domain operations, not direct data manipulation. This reduces test brittleness and improves domain clarity.

## Two categories of tests

**Data-oriented tests**: Set up expected data directly, verify storage state afterward.

**Domain-oriented tests**: Set up input data, modify through domain operations, verify system responds correctly.

## The problem with data-oriented setup

```kotlin
@Test
fun testDataOrientedTest() {
    val application = Application.valid()

    applicationRepo.addApplication(application)  // Direct data manipulation
    applicationService.approveApplication(application.id)

    assertThat(applicationRepo.getApplication(application.id).status)
        .isEqualTo(ApplicationStatus.APPROVED)
}
```

This test passes. But consider new requirements: **Approval needs to check whether you are still an active customer.**

The data-oriented test fails with a null pointer in `approveApplication()` because no customer exists. The test was setting up data that assumed a state that domain logic now requires differently.

## Domain-oriented setup survives change

```kotlin
@Test
fun testDomainOrientedTest() {
    val application = Application.valid()

    applicationService.registerInitialApplication(application)  // Domain operation
    applicationService.approveApplication(application.id)

    assertThat(applicationRepo.getApplication(application.id).status)
        .isEqualTo(ApplicationStatus.APPROVED)
}
```

When the requirement changes, `registerInitialApplication` handles customer creation internally. The test keeps working because it uses the domain, not assumptions about internal data.

Note: As the domain evolves further, the method signature may change too — e.g., requiring a `Customer` argument (`registerInitialApplication(customer, application)`). Even then, the compiler guides you to update test setup, and the fix is straightforward. The data-oriented test, by contrast, fails at runtime with a confusing null pointer.

## Benefits

- **Tests survive refactoring**: Logic changes don't break unrelated tests
- **Better domain design**: Writing tests forces clear domain operations
- **Clearer tests**: Tests read as domain scenarios, not data manipulation
- **Non-nullable fields**: Domain operations create consistent state with real values
- **Fewer irrelevant failures**: Tests only fail when relevant behavior changes

## When domain-oriented setup shines

### Scenario: Expiring old applications

Data-oriented (brittle):
```kotlin
@Test
fun shouldExpireOldApplications() {
    val application = Application.valid().copy(
        applicationDate = LocalDate.now().minusMonths(7),
        status = ApplicationStatus.ACTIVE
    )
    applicationRepo.addApplication(application)
    // If expiration logic changes to check more fields, this breaks

    applicationService.expireApplications()

    assertThat(applicationRepo.getApplication(application.id).status)
        .isEqualTo(ApplicationStatus.EXPIRED)
}
```

Domain-oriented (resilient):
```kotlin
@Test
fun shouldExpireOldApplications() {
    with(testContext) {
        clock.setTo(LocalDate.of(2022, 1, 1))
        val customer = Customer.valid()
        val application = Application.valid(customer.id)

        applicationService.registerInitialApplication(customer, application)

        clock.advance(Duration.ofDays(7 * 30))  // 7 months pass
        applicationService.expireApplications()

        assertThat(applicationService.activeApplicationFor(application.name))
            .doesNotContain(application)
    }
}
```

The domain-oriented test uses time manipulation and domain operations. When expiration logic changes, the test automatically handles the new requirements.

## Guidelines

### Do
- Use service methods to set up state
- Use TestClock to control time
- Assert on domain outcomes, not database state
- Let domain operations handle data relationships

### Don't
- Directly manipulate repositories in test setup
- Assume what data state should look like
- Verify database fields when you can verify behavior
- Duplicate domain logic in test setup

## Combining with Object Mother

Object Mother creates valid input objects. TTTD uses domain operations to get them into the system.

```kotlin
@Test
fun shouldRejectApplicationWhenCustomerInactive() {
    with(testContext) {
        val customer = Customer.valid().copy(active = false)  // Object Mother + variation
        val application = Application.valid(customer.id)

        applicationService.registerInitialApplication(customer, application)  // TTTD

        assertThat(applicationService.approveApplication(application.id))
            .isFailure()
    }
}
```

Object Mother provides the *what* (test data). TTTD handles the *how* (getting state into the system).

## Extracting setup helpers

As setup grows, extract to helper methods that still use the domain:

```kotlin
// Test helper that uses domain methods
fun SystemTestContext.createApprovedApplication(): Application {
    val customer = Customer.valid()
    val application = Application.valid(customer.id)
    applicationService.registerInitialApplication(customer, application)
    applicationService.approveApplication(application.id)
    return applicationService.getApplication(application.id)
}

// Test stays focused
@Test
fun approvedApplicationCanBeRenewed() {
    with(testContext) {
        val approved = createApprovedApplication()

        applicationService.renewApplication(approved.id)

        assertThat(repositories.applicationRepo.getApplication(approved.id).status)
            .isEqualTo(ApplicationStatus.RENEWED)
    }
}
```

These helpers still use domain methods (`registerInitialApplication`, `approveApplication`) rather than directly manipulating repositories.

## Decision guide

**Use domain methods when:**
- Setting up entity state that goes through domain logic
- State transitions involve validation or business rules
- Multiple entities need to be created together (aggregates)

**Direct infrastructure is OK when:**
- Testing the infrastructure itself (repository query logic)
- Setting up unrelated background data (test fixtures)
- The domain method doesn't exist and creating it would be artificial

## Anti-patterns

**Bypassing validation** -- Don't create impossible state just because it's convenient:
```kotlin
// BAD: Creates impossible state
applicationRepo.save(Application(status = APPROVED, approvalDate = null))

// GOOD: Use domain method that maintains invariants
applicationService.registerInitialApplication(application)
applicationService.approveApplication(application.id)
```

**Over-testing implementation** -- Don't verify intermediate repository calls:
```kotlin
// BAD: Coupled to implementation (Mockito syntax)
verify(applicationRepo).save(any())

// GOOD: Verify outcomes
assertThat(applicationRepo.getApplication(id).status).isEqualTo(APPROVED)
```

**Creating "test-only" domain methods** -- If you need a domain method only for tests, that's a smell:
```kotlin
// BAD: Method only exists for tests
applicationService.createApplicationInApprovedState(...)

// GOOD: Use the real workflow
applicationService.registerInitialApplication(...)
applicationService.approveApplication(...)
```

## Evolution path

1. Start with data-oriented tests (quick to write)
2. When tests break due to logic changes, refactor to TTTD
3. Build domain operations that support test setup
4. Domain becomes clearer as a side effect
5. New tests naturally use TTTD because the patterns exist
