# TDD Concepts Overview

> [!NOTE]
> This is a completely AI generated file.
> But it did visualize some relationships in a good way, so I decided to keep it.
> Proceed with caution.
> :-)

This document provides a comprehensive visual overview of how all TDD concepts in this repository relate to each other and work together to create maintainable, fast, and reliable tests.

## Concept Relationships

### 1. Foundation: TDD Cycle

```mermaid
stateDiagram-v2
    [*] --> Red: Write failing test
    Red --> Green: Write minimal code
    Green --> Refactor: Improve design
    Refactor --> Red: Next feature
    Refactor --> [*]: Feature complete

    note right of Red
        Use Test Setup patterns
        Use .valid() extensions
        Use SystemTestContext
    end note

    note right of Green
        Implement just enough
        Use fakes for dependencies
    end note

    note right of Refactor
        Extract methods
        Remove duplication
        Tests protect changes
    end note
```

### 2. Test Setup Flow

```mermaid
flowchart TD
    Start[Write New Test] --> NeedData{Need test data?}
    NeedData -->|Yes| ExistingExt{Extension exists?}
    NeedData -->|No| NeedDeps

    ExistingExt -->|Yes| UseValid[Use .valid<br/>with .copy for variations]
    ExistingExt -->|No| CreateExt[Create extension<br/>on companion object]

    UseValid --> NeedDeps{Need dependencies?}
    CreateExt --> NeedDeps

    NeedDeps -->|Yes| UseContext[Use SystemTestContext<br/>for standard setup]
    NeedDeps -->|Special case| ManualDI[Manual DI<br/>for specific config]
    NeedDeps -->|No| WriteTest

    UseContext --> WriteTest[Write Test]
    ManualDI --> WriteTest

    WriteTest --> NeedFakes{External deps?}
    NeedFakes -->|Yes| FakesAvail{Fakes exist?}
    NeedFakes -->|No| TestDone

    FakesAvail -->|Yes| UseFake[Use existing fake]
    FakesAvail -->|No| CreateFake[Create fake<br/>HashMap implementation]

    UseFake --> TestDone[Test Complete]
    CreateFake --> TestDone

    style Start fill:#e1f5fe,color:#000
    style TestDone fill:#c8e6c9,color:#000
    style UseValid fill:#fff9c4,color:#000
    style UseContext fill:#f8bbd0,color:#000
```

### 3. Fake Usage Pattern

```mermaid
flowchart LR
    subgraph "Production Code"
        Service[Service Layer]
        RepoInterface[Repository<br/>Interface]
        ClientInterface[Client<br/>Interface]
    end

    subgraph "Production Implementation"
        RepoImpl[Real Repository<br/>PostgreSQL/MySQL]
        ClientImpl[Real Client<br/>HTTP/gRPC]
    end

    subgraph "Test Implementation"
        RepoFake[Repository Fake<br/>HashMap]
        ClientFake[Client Fake<br/>In-memory]
    end

    subgraph "Test Features"
        ErrorInject[Error Injection<br/>registerFailure]
        StateVerify[State Verification<br/>getNotifications]
        FastExec[Fast Execution<br/>No I/O]
    end

    Service --> RepoInterface
    Service --> ClientInterface

    RepoInterface -.->|Production| RepoImpl
    RepoInterface -.->|Testing| RepoFake

    ClientInterface -.->|Production| ClientImpl
    ClientInterface -.->|Testing| ClientFake

    RepoFake --> ErrorInject
    RepoFake --> StateVerify
    RepoFake --> FastExec

    ClientFake --> ErrorInject
    ClientFake --> StateVerify
    ClientFake --> FastExec

    style Service fill:#e1f5fe,color:#000
    style RepoFake fill:#c8e6c9,color:#000
    style ClientFake fill:#c8e6c9,color:#000
```

### 4. Testing Through the Domain

```mermaid
sequenceDiagram
    participant Test
    participant Service
    participant AppRepo as Application<br/>Repo (Fake)
    participant CustRepo as Customer<br/>Repo (Fake)

    Note over Test,CustRepo: ❌ Data-Oriented Approach (Brittle)
    Note over Test: Create test data
    Test->>AppRepo: addApplication(application)
    Note over Test,CustRepo: Directly manipulate storage<br/>bypassing domain logic
    Test->>Service: approveApplication(id)
    Service->>CustRepo: getCustomer(customerId)
    CustRepo-->>Service: null (customer not stored!)
    Note over Service: ❌ NullPointerException<br/>Test breaks when new requirement<br/>added (customer storage)

    Note over Test,CustRepo: ✅ Domain-Oriented Approach (Resilient)
    Note over Test: Create test data
    Test->>Service: registerInitialApplication(customer, app)
    Service->>CustRepo: addCustomer(customer)
    Service->>AppRepo: addApplication(app)
    Note over Test,CustRepo: Domain method ensures<br/>all invariants are met
    Test->>Service: approveApplication(id)
    Service->>CustRepo: getCustomer(customerId)
    CustRepo-->>Service: customer (exists!)
    Service->>AppRepo: updateApplication(APPROVED)
    Note over Test: ✅ Test survives changes<br/>Domain logic maintains consistency
```

### 5. Test Type Decision Tree

```mermaid
flowchart TD
    Start[What to Test?] --> Logic{Pure business logic?}

    Logic -->|Yes| DomainTest[Domain Test<br/>No fakes<br/>Pure functions]
    Logic -->|No| External{External I/O?}

    External -->|Database| DBTest{Testing SQL<br/>or ORM?}
    External -->|HTTP/gRPC| HTTPTest{Testing protocol<br/>or client code?}
    External -->|No| BusinessFlow

    DBTest -->|Yes| IOTestDB[IO Test<br/>Real DB<br/>Test SQL correctness]
    DBTest -->|No| UseFakeDB[Use Fake<br/>Test business logic]

    HTTPTest -->|Yes| IOTestHTTP[IO Test<br/>Mock HTTP engine<br/>Test protocol]
    HTTPTest -->|No| UseFakeHTTP[Use Fake<br/>Test business logic]

    BusinessFlow{Multiple components<br/>interacting?}
    BusinessFlow -->|Yes| OutcomeTest[Outcome Test<br/>With fakes<br/>End-to-end scenario]
    BusinessFlow -->|No| VarTest[Variation Test<br/>With fakes<br/>Edge cases]

    DomainTest --> FastSuite[Fast Test Suite]
    IOTestDB --> FastSuite
    IOTestHTTP --> FastSuite
    UseFakeDB --> FastSuite
    UseFakeHTTP --> FastSuite
    OutcomeTest --> FastSuite
    VarTest --> FastSuite

    style Start fill:#e1f5fe,color:#000
    style DomainTest fill:#c8e6c9,color:#000
    style IOTestDB fill:#fff9c4,color:#000
    style IOTestHTTP fill:#fff9c4,color:#000
    style UseFakeDB fill:#c8e6c9,color:#000
    style UseFakeHTTP fill:#c8e6c9,color:#000
    style OutcomeTest fill:#f8bbd0,color:#000
    style VarTest fill:#f8bbd0,color:#000
    style FastSuite fill:#c8e6c9,color:#000
```

## Key Takeaways

### The Three Pillars Work Together

1. **Test Setup** provides the foundation
   - Easy data creation with `.valid()` extensions
   - Consistent system setup with `SystemTestContext`
   - Reusable test helpers and DSLs

2. **Fakes** enable fast, reliable testing
   - In-memory implementations with HashMap
   - Configurable behavior for error scenarios
   - State inspection without mocking frameworks

3. **Testing Through the Domain** reduces brittleness
   - Use service methods to set up state
   - Verify outcomes, not implementation details
   - Tests survive refactoring

### Benefits Cascade

```mermaid
flowchart LR
    TestSetup[Test Setup<br/>Easy data creation] --> Write[Easy to Write<br/>Tests]
    Fakes[Fakes<br/>In-memory] --> Fast[Fast<br/>Execution]
    TTTD[Testing Through Domain<br/>Use services] --> Maintain[Easy to<br/>Maintain]

    Write --> Productive[Productive<br/>Development]
    Fast --> Productive
    Maintain --> Productive

    Productive --> Confidence[High<br/>Confidence]
    Productive --> Frequent[Frequent<br/>Releases]

    Confidence --> Success[Successful<br/>TDD Practice]
    Frequent --> Success

    style TestSetup fill:#e8f5e9,color:#000
    style Fakes fill:#fff3e0,color:#000
    style TTTD fill:#fce4ec,color:#000
    style Success fill:#c8e6c9,color:#000
```

## Workshop Journey

The workshop exercises build on these concepts progressively:

```mermaid
timeline
    title Workshop Learning Path
    section Exercise 1
        Test Setup : Object Mother pattern
                   : .valid() extensions
                   : Arrange-Act-Assert
        SystemContext : Understanding DI setup
                     : Using testContext
    section Exercise 2
        Fakes : Creating fakes
              : Error injection
              : State verification
        Test Helpers : DSLs for setup
                    : Clock manipulation
    section Exercise 3
        Manual DI : Understanding contexts
                 : When to go manual
        Async Testing : runTest and coroutines
                     : Dispatcher behavior
        Mocking : When to use mocks
               : HTTP protocol testing
```

## References

- [TDD Overview](tdd.md) - Philosophy and approach
- [Test Setup](test-setup.md) - Data and system setup patterns
- [Fakes](fakes.md) - Creating and using test fakes
- [Testing Through the Domain](tttd.md) - Domain-oriented testing
- [Manual Dependency Injection](manual-dependency-injection.md) - DI for tests
- [Workshop Exercises](workshop/README.md) - Hands-on practice
- [Exercise Answers](workshop/exercise-answers.md) - Detailed solutions

## Visual Cheat Sheet

### Quick Decision Guide

```mermaid
flowchart LR
    Start{Starting a Test?}
    Start -->|Need data| Valid[Use .valid<br/>+ .copy]
    Start -->|Need system| Context[Use SystemTestContext]
    Start -->|Need external deps| Fake[Check for Fakes<br/>or create one]
    Start -->|Setup state| Domain[Use service methods<br/>not direct data]

    Valid --> Test[Write Test]
    Context --> Test
    Fake --> Test
    Domain --> Test

    Test --> Verify{What to verify?}
    Verify -->|State| Query[Use domain queries<br/>or repo.get]
    Verify -->|Interaction| FakeMethod[Use fake's<br/>custom methods]

    style Start fill:#e1f5fe,color:#000
    style Test fill:#f8bbd0,color:#000
    style Query fill:#c8e6c9,color:#000
    style FakeMethod fill:#c8e6c9,color:#000
```

### Pattern Selection

| Scenario | Pattern | Example |
|----------|---------|---------|
| Need test data | Object Mother | `Application.valid()` |
| Complex variations | Helper parameters | `Application.valid(monthsOld=7)` |
| Simple variations | Copy method | `.copy(status=DENIED)` |
| Need system setup | Test Context | `SystemTestContext()` |
| Special DI config | Manual DI | Create services manually |
| External dependency | Fake | `ApplicationRepositoryFake()` |
| Test errors | Error injection | `fake.registerFailure(id)` |
| Verify interactions | Fake methods | `fake.getNotifications()` |
| Setup test state | Domain methods | `service.registerApplication()` |
| Verify outcomes | Domain queries | `service.isExpired(id)` |

---

**Remember**: These patterns work best when combined. Use test setup for easy data creation, fakes for fast execution, and domain-oriented testing for maintainability. Together, they make TDD predictable and easy.
