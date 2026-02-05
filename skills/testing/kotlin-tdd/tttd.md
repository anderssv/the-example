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
    appService.approveApplication(application.id)

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

    appService.registerInitialApplication(application)  // Domain operation
    appService.approveApplication(application.id)

    assertThat(applicationRepo.getApplication(application.id).status)
        .isEqualTo(ApplicationStatus.APPROVED)
}
```

When the requirement changes, `registerInitialApplication` handles customer creation. The test keeps working because it uses the domain, not assumptions about internal data.

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

    appService.expireApplications()

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

## Evolution path

1. Start with data-oriented tests (quick to write)
2. When tests break due to logic changes, refactor to TTTD
3. Build domain operations that support test setup
4. Domain becomes clearer as a side effect
5. New tests naturally use TTTD because the patterns exist
