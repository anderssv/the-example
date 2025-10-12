# TDD Workshop Summary

Based on the materials provided, the TDD (Test-Driven Development) workshop is a comprehensive 4-hour session designed to help developers improve their testing practices and create more maintainable code. The workshop combines theoretical concepts with hands-on exercises using Kotlin examples.

## Getting Started

### Visual Overview
📊 [TDD Concepts Overview](../tdd-concepts-overview.md) - Comprehensive visual guide showing how all concepts relate and work together

### Workshop Exercises
1. [Exercise 1](../../src/test/kotlin/workshop/Exercise1Test.kt) - Test data setup and Arrange-Act-Assert
2. [Exercise 2](../../src/test/kotlin/workshop/Exercise2Test.kt) - Fakes, helpers, and DSLs
3. [Exercise 3](../../src/test/kotlin/workshop/Exercise3Test.kt) - Manual DI and async testing

### Answer Key
- [Complete answers and discussion guide](exercise-answers.md)
- Answer files: Exercise1TestAnswer.kt, Exercise2TestAnswer.kt, Exercise3TestAnswer.kt

### Prerequisites
- Basic Kotlin knowledge
- IDE with Kotlin support (IntelliJ IDEA recommended)
- Project cloned and building successfully (see [main README](../../README.md))

## Workshop Structure

### Part 1: Introduction to TDD
- **Core Concepts**: Explanation of TDD philosophy, the red-green-refactor cycle, and the benefits of writing tests before code
  - Reference: [TDD Overview](../tdd.md)
- **Test Coverage**: Discussion on appropriate levels of test coverage and when TDD is most beneficial
- **Test Types**: Overview of different test levels (unit, integration, system, etc.)
- **Test Data Management**: Introduction to Object Mother pattern and test data builders for reusable test setup
  - Reference: [Test Setup](../test-setup.md)
- **Exercise 1**: Practical session on setting up test data and implementing the Arrange-Act-Assert pattern
  - Code: [Exercise1Test.kt](../../src/test/kotlin/workshop/Exercise1Test.kt)
  - Example: [ApplicationFakeTest.kt](../../src/test/kotlin/application/ApplicationFakeTest.kt)
  - Answers: [exercise-answers.md](exercise-answers.md#exercise-1---bootup-test-data-and-arrange-act-assert)

### Part 2: Testing Techniques
- **Test Doubles**: Detailed explanation of fakes, mocks, and stubs with emphasis on fakes for maintainable tests
  - Reference: [Fakes](../fakes.md)
  - Examples: [UserNotificationClientFake.kt](../../src/test/kotlin/notifications/UserNotificationClientFake.kt), [ApplicationRepositoryFake.kt](../../src/test/kotlin/application/ApplicationRepositoryFake.kt)
- **Testing Through the Domain**: Approach for writing domain-oriented tests that are less brittle to implementation changes
  - Reference: [Testing Through the Domain](../tttd.md)
  - Example: [TestingThroughTheDomainTest.kt](../../src/test/kotlin/application/TestingThroughTheDomainTest.kt)
- **Test DSLs**: Creating domain-specific languages for more readable and maintainable tests
- **Test Helpers**: Implementation of utility classes to simplify test setup
  - Example: [TestExtensions.kt](../../src/test/kotlin/application/TestExtensions.kt)
- **Exercise 2**: Hands-on practice implementing fakes, test helpers, and working with the test clock
  - Code: [Exercise2Test.kt](../../src/test/kotlin/workshop/Exercise2Test.kt)
  - Answers: [exercise-answers.md](exercise-answers.md#exercise-2---fakes-helpers-and-dsls)

### Part 3: Testable Architecture
- **Manual Dependency Injection**: Implementation and benefits over frameworks
  - Reference: [Manual Dependency Injection](../manual-dependency-injection.md)
  - Examples: [SystemContext.kt](../../src/main/kotlin/system/SystemContext.kt), [SystemTestContext.kt](../../src/test/kotlin/system/SystemTestContext.kt)
- **Simple Architecture**: Designing systems that are inherently testable
  - Reference: [System Design](../system-design.md)
  - Diagram: [exercise-info.md](exercise-info.md)
- **Observability**: Incorporating logs, metrics, and traces
- **Performance Considerations**: Techniques for faster tests including streaming, profiling
- **Exercise 3**: Implementing manual dependency injection and working with asynchronous code testing
  - Code: [Exercise3Test.kt](../../src/test/kotlin/workshop/Exercise3Test.kt)
  - Example: [BrregClientTest.kt](../../src/test/kotlin/brreg/BrregClientTest.kt)
  - Answers: [exercise-answers.md](exercise-answers.md#exercise-3---manual-di-mocking-and-async-testing)

### Conclusion
- Summary of key learnings
- Q&A session
- Guidance on applying these concepts to participants' own codebases

## Key Themes Throughout the Workshop

1. **Maintainability**: Techniques to make tests that break only when they should, not due to unrelated changes
2. **Speed**: Ensuring tests run quickly enough to be part of a tight feedback loop
3. **Reusability**: Patterns for creating test components that can be reused across the test suite
4. **Readability**: Making tests that clearly express their intent and serve as documentation
5. **Practical Implementation**: Real-world examples in Kotlin showing how to implement these patterns

The workshop demonstrates these concepts using a sample application that includes:
- An application service with repositories
- Customer registration functionality
- Test fakes for different components
- Test helpers for clock manipulation and data setup
- Examples of manual dependency injection

The material emphasizes pragmatic approaches to testing that help developers create maintainable test suites that provide value over time rather than becoming a burden during refactoring.
