# Workshop Exercise Answers and Discussion Points

This document provides answers to the questions posed in each exercise and discussion points for workshop facilitators.

## Exercise 1 - Bootup, Test Data and Arrange-Act-Assert

### Test 1: shouldRegisterApplicationAndStoreCorrectly

**Questions:**
- **How do we set up and re-use test data?**

  **Answer:** Use extension functions on companion objects (e.g., `Customer.valid()`, `Application.valid()`). This provides:
  - A single, discoverable location for test data setup
  - Sensible defaults that work for most tests
  - Easy customization through parameters or `.copy()` for specific test needs
  - Reduces coupling between tests and data structures

  **Example:**
  ```kotlin
  val customer = Customer.valid()  // Default valid customer
  val application = Application.valid(customerId = customer.id)  // Default application
  ```

- **What is the best abstraction to verify that the application was stored correctly?**

  **Answer:** Query the repository directly or use domain queries:
  - **Repository approach:** `repositories.applicationRepo.getApplication(application.id)` - Tests the exact storage
  - **Domain approach:** `applicationService.applicationsForName(application.name)` - Tests through domain behavior

  The repository approach is more direct for this test, but domain queries are better for testing business logic outcomes.

**Discussion Points:**
- Explain the Arrange-Act-Assert pattern and why it makes tests more readable
- Discuss when to use repository verification vs. domain queries
- Show how test data extensions reduce test maintenance burden

### Test 2: shouldRegisterAndApplicationAndModifyForTesting

**Questions:**
- **How can we re-use test data setup and make it tunable in each test?**

  **Answer:** Combine `.valid()` with `.copy()` for customization:
  ```kotlin
  val application = Application.valid(customerId = customer.id).copy(
      name = "Custom Name",
      applicationDate = LocalDate.of(2023, 1, 1)
  )
  ```

  For complex modifications, add helper parameters to `.valid()`:
  ```kotlin
  val application = Application.valid(customerId = customer.id, monthsOld = 7)
  ```

- **How do we signify what are the important changes for that test?**

  **Answer:**
  - Use `.copy()` in the test to highlight what's different from the default
  - Only modify fields that are relevant to the test scenario
  - Use meaningful variable names and comments when needed
  - The modifications signal to readers: "This is what matters for this test"

- **Should we use data on the application to verify or ask questions?**

  **Answer:** Ask questions about the domain state rather than checking raw data when possible:
  - ❌ Less clear: `assertThat(application.status).isEqualTo(ApplicationStatus.EXPIRED)`
  - ✅ More clear: `assertThat(applicationService.isExpired(application.id)).isTrue()`

  However, for simple status checks, direct field access is fine. The key is to verify business outcomes, not implementation details.

**Discussion Points:**
- Show examples of when helper parameters in `.valid()` are better than `.copy()`
- Discuss how highlighting only changed fields makes tests self-documenting
- Explain "Testing Through the Domain" concept (will be expanded in later exercises)

### Test 3: shouldThrowExceptionWhenApprovingDeniedApplication

**Questions:**
- **Is this a normal control flow or an exceptional case?**

  **Answer:** This is an exceptional case. Attempting to approve a denied application represents a programming error or invalid state transition, not normal business logic flow. Exceptions are appropriate here.

- **What solutions are alternatives here?**

  **Answer:** Several alternatives:
  1. **Exception (current approach):** Good for truly exceptional cases
  2. **Result/Either type:** Return `Result<Application, Error>` - Better for expected failure cases
  3. **Boolean return:** Return true/false - Too simple, loses information
  4. **Sealed class:** Return a sealed class with Success/Failure states - Excellent for complex cases

  ```kotlin
  sealed class ApprovalResult {
      data class Success(val application: Application) : ApprovalResult()
      data class InvalidState(val reason: String) : ApprovalResult()
  }
  ```

- **Can or should we encode the arrange, act, assert steps?**

  **Answer:** For exception testing, combining Act and Assert is acceptable:
  ```kotlin
  assertThrows(IllegalStateException::class.java) {
      applicationService.approveApplication(application.id)
  }
  ```

  However, if you need to verify the exception message or cause, separate them:
  ```kotlin
  val exception = assertThrows(IllegalStateException::class.java) {
      applicationService.approveApplication(application.id)
  }
  assertThat(exception.message).contains("cannot approve denied application")
  ```

**Discussion Points:**
- Compare exceptions vs. Result types in Kotlin
- Discuss when to use sealed classes for complex state machines
- Show how functional error handling can make code more maintainable

---

## Exercise 2 - Fakes, Helpers, and DSLs

### Test 1: shouldStoreApplication

**Questions:**
- **When is this worth it?**

  **Answer:** Helper DSLs are worth it when:
  - You have complex setup that's repeated across many tests
  - The setup involves multiple steps (create customer, create application, register both)
  - The setup obscures the "interesting part" of the test
  - You want to provide sensible defaults while allowing customization

  **Not worth it when:**
  - Setup is simple and only used once or twice
  - The DSL would be more complex than inline setup
  - The abstraction hides important details needed to understand the test

- **Could it be done with the assertions as well?**

  **Answer:** Yes! You can create assertion DSLs:
  ```kotlin
  fun Application.shouldBeActive() {
      assertThat(this.status).isEqualTo(ApplicationStatus.ACTIVE)
  }

  fun Application.shouldHaveName(expectedName: String) {
      assertThat(this.name).isEqualTo(expectedName)
  }

  // Usage:
  application.shouldBeActive()
  application.shouldHaveName("Test User")
  ```

  This can improve readability but use sparingly - standard assertions are often clearer.

**Discussion Points:**
- Show examples of when DSLs improve vs. harm test readability
- Discuss the balance between DRY (Don't Repeat Yourself) and explicit test setup
- Compare DSL approach vs. factory methods vs. builder patterns

### Test 2: shouldExpireApplicationAfter6Months

**Questions:**
- **How would you do this with mocks?**

  **Answer:** With mocks, you'd need to:
  ```kotlin
  val mockNotificationClient = mock<UserNotificationClient>()
  // ... setup service with mock ...

  // Verify the mock was called
  verify(mockNotificationClient).notifyUser(
      eq(application.id),
      eq(application.name),
      contains("expired")
  )
  ```

- **How would mocks fare if something changed?**

  **Answer:** Mocks are brittle to changes:

  **Scenario 1:** If notification format changes:
  - **Mock:** Every test with `contains("expired")` breaks
  - **Fake:** Only tests that check notification content break (fewer tests)

  **Scenario 2:** If we add a new parameter to `notifyUser()`:
  - **Mock:** All mock setups break (verify calls need updating)
  - **Fake:** Implement once in the fake, all tests continue working

  **Scenario 3:** If we batch notifications instead of sending individually:
  - **Mock:** All verification logic breaks (expected call counts wrong)
  - **Fake:** Tests checking outcomes still pass; only interaction-checking tests break

**Discussion Points:**
- Show a side-by-side comparison of mock vs. fake for the same test
- Discuss when mocks are appropriate (testing 3rd party integrations, protocol verification)
- Explain the maintenance cost difference over time

### Test 3: shouldFailToNotifyForSpecificApplication

**Questions:**
- **How does this differ from stubs?**

  **Answer:**
  - **Stub:** Returns pre-programmed responses, but doesn't verify calls or maintain state
  - **Fake:** Has working implementation with state (stores notifications, tracks failures)
  - **This test:** Uses fake's ability to register failures, demonstrating stateful behavior

  The fake can be "programmed" to fail for specific inputs while still maintaining its state and normal behavior for other inputs.

- **How would you do this with mocks?**

  **Answer:**
  ```kotlin
  val mockClient = mock<UserNotificationClient>()
  whenever(mockClient.notifyUser(eq(application.id), any(), any()))
      .thenThrow(IOException("Simulated failure"))
  ```

  The mock approach is more verbose and requires understanding mocking framework syntax.

- **What would the diff look like if you rewrote a test from mock to fakes?**

  **Answer:** See example diff:
  ```diff
  - val mockClient = mock<UserNotificationClient>()
  - whenever(mockClient.notifyUser(any(), any(), any())).thenReturn(Unit)
  + val fakeClient = UserNotificationClientFake()

    val service = ApplicationService(
  -     userNotificationClient = mockClient,
  +     userNotificationClient = fakeClient,
      // ... other deps ...
    )

  - verify(mockClient, times(1)).notifyUser(
  -     eq(application.id),
  -     eq(application.name),
  -     contains("approved")
  - )
  + assertThat(fakeClient.getNotificationForUser(application.name))
  +     .contains("Your application ${application.id} has been approved")
  ```

**Discussion Points:**
- Demonstrate writing a fake from scratch (15-20 lines for most cases)
- Show how fakes are reusable across all tests without additional setup
- Discuss error injection patterns in fakes

---

## Exercise 3 - Manual DI, Mocking and Async Testing

### Test 1: shouldStoreApplicationWithoutUsingSystemContext

**Questions:**
- **What is the benefit of using the SystemContext?**

  **Answer:**
  - **Reduced boilerplate:** One line setup vs. creating all dependencies
  - **Consistency:** All tests use the same dependency configuration
  - **Shared helpers:** Access to clock manipulation, shared fakes
  - **Easy to extend:** Add new dependencies once, available everywhere
  - **Mimics production:** Same structure as production DI setup

- **What is the benefit of setting up all dependencies manually?**

  **Answer:**
  - **Explicit:** Clear what dependencies are being used
  - **Isolated:** Test doesn't depend on SystemContext changes
  - **Custom configuration:** Easy to inject specific implementations or configurations
  - **Learning:** Forces understanding of dependency structure
  - **Precision:** Only create what you need for this specific test

- **How much should you set up manually?**

  **Answer:** Balance between:
  - **Common tests (90%):** Use SystemContext for consistency and speed
  - **Infrastructure tests:** Manual setup to test specific configurations
  - **Integration tests:** Manual setup when you need specific real implementations
  - **Edge cases:** Manual when you need unusual dependency combinations

  **Rule of thumb:** Use SystemContext by default; go manual only when you need something special.

**Discussion Points:**
- Show the production SystemContext and how it mirrors the test version
- Discuss manual DI vs. frameworks like Spring or Koin
- Explain when compile-time vs. runtime DI is appropriate

### Test 2: testThatTheClientCodeBehavesAsExpectedOn404Responses

**Questions:**
- **When would you use mocking instead of a fake implementation?**

  **Answer:** Use mocks when:
  - Testing protocol-level behavior (HTTP status codes, headers)
  - Testing 3rd party libraries where you can't control the implementation
  - Testing error conditions that are hard to reproduce (network timeouts)
  - You need to verify the exact sequence or parameters of calls
  - Creating a fake would require significant complexity

  **Use fakes when:**
  - Testing business logic and outcomes
  - The interface is under your control
  - You need reusable test doubles
  - State management is important

- **Is it important to verify the request URL in this test?**

  **Answer:** It depends:
  - **For this test:** Yes, because we're testing the client implementation itself
  - **For higher-level tests:** No, we trust the client works and focus on business logic

  URL verification ensures the client constructs requests correctly, which is important at the HTTP layer.

- **What are the trade-offs between mocking HTTP responses and using real HTTP calls in tests?**

  **Answer:**

  | Aspect | Mocked HTTP | Real HTTP |
  |--------|-------------|-----------|
  | **Speed** | Fast (milliseconds) | Slow (seconds) |
  | **Reliability** | Always works | Flaky (network, service availability) |
  | **Coverage** | Tests your code | Tests integration |
  | **Setup** | Complex mock configuration | Simple, but needs test environment |
  | **Confidence** | Lower (didn't test real integration) | Higher (full stack tested) |

  **Best practice:** Use mocked HTTP for most tests, real HTTP for a small subset of integration tests.

**Discussion Points:**
- Show how to set up WireMock or similar for more realistic HTTP testing
- Discuss the testing pyramid (unit → integration → e2e)
- Explain contract testing as an alternative approach

### Test 3: testSomethingAsync and testSomethingAsync2

**Questions:**
- **Do fakes have to be async?**

  **Answer:** No! Fakes should almost never be async unless:
  - The real implementation is async and the interface requires it
  - You're specifically testing async behavior

  **Why avoid async in fakes:**
  - In-memory operations are instantaneous
  - Async adds complexity without benefit
  - Tests run slower
  - Harder to debug

  **Keep fakes synchronous** and fast. If the real implementation is async, the fake can return immediately completed futures/coroutines.

- **If you have to wait, can you avoid blocking?**

  **Answer:** Yes, in Kotlin:
  - Use `runTest` for coroutine tests - it skips delays automatically
  - Use `TestDispatcher` to control time virtualization
  - Switch to `Dispatchers.IO` only when you need real delays

  ```kotlin
  runTest {
      delay(1.hours)  // Completes instantly in test
  }
  ```

- **How parallel can you run tests?**

  **Answer:**
  - **Unit tests:** Highly parallel (limited by CPU cores)
  - **With fakes:** Fully parallel (no shared state, no I/O)
  - **With real databases:** Limited parallelism (connection pools, data isolation)
  - **With async:** Be careful with dispatchers (default test dispatcher is single-threaded)

  **This project:** Tests run in parallel using JUnit's parallel execution (configured in build.gradle.kts)

**Discussion Points:**
- Demonstrate how `runTest` virtualizes time
- Show test execution time with and without parallelization
- Discuss strategies for handling shared state in parallel tests
- Explain different dispatchers (Default, IO, Main, Unconfined)

---

## Common Misconceptions to Address

### 1. "Fakes are too much work"
**Reality:** A typical fake is 15-30 lines using a HashMap. The upfront cost is minimal, and the maintenance savings are substantial.

### 2. "I need to verify interactions, so I need mocks"
**Reality:** You usually want to verify outcomes, not interactions. Fakes enable outcome verification. Reserve interaction verification for protocol testing.

### 3. "Test data factories create coupling"
**Reality:** Properly designed `.valid()` extensions reduce coupling by centralizing data creation. Changes to domain models require updating one place instead of hundreds of tests.

### 4. "DSLs make tests harder to understand"
**Reality:** Good DSLs make tests more readable. Bad DSLs hide important details. The key is balance and clear naming.

### 5. "Manual DI is outdated"
**Reality:** For small to medium projects, manual DI provides better compile-time safety, easier testing, and less magic. Use frameworks when you need their specific features.

---

## Workshop Facilitation Tips

### Timing
- **Exercise 1:** 30-40 minutes (including discussion)
- **Exercise 2:** 40-50 minutes (DSL can be tricky)
- **Exercise 3:** 30-40 minutes (async concepts may need extra explanation)

### Common Sticking Points

1. **Exercise 1:** Students often forget to register the customer before creating the application
2. **Exercise 2:** The DSL lambda syntax can be confusing - have examples ready
3. **Exercise 3:** Understanding dispatcher behavior in tests requires practice

### Live Coding Suggestions

For each exercise:
1. Have participants attempt on their own (10-15 min)
2. Live code the solution together, pausing for questions
3. Discuss the questions and alternatives
4. Show the answer file as a reference

### Extended Discussions

If time permits, explore:
- Property-based testing with fakes
- Testing strategies for different architectural patterns
- How to introduce these patterns to existing codebases
- Cost/benefit analysis of different testing approaches
