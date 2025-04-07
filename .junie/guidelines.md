# Project Guidelines

## Tech Stack
- Kotlin on the JVM
- Gradle (Kotlin DSL) for build management
- JUnit and AssertJ for testing
- Jackson for JSON handling

## Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "fully.qualified.TestClassName"
```

## Development Setup
1. Install prerequisites:
   - ASDF version manager
   - Java 21 (via ASDF)
   - Git

2. Build project:
   ```bash
   ./gradlew build
   ```
3. Print available dependency updates
   ```
   ./gradlew dependencyUpdates
   ``` 

## Best Practices
1. **Testing**
   - Write tests before the implementation and let that guide you implementing (TDD approach)
   - Use fakes instead of mocks when possible
   - Follow Arrange-Act-Assert pattern
   - Re-use test data setup, prefer <class>.valid() test extension methods.

2. **Code Organization**
   - Follow domain-driven package structure
   - Keep services focused and small
   - Use dependency injection for better testability
   - Maintain clear separation between domain and infrastructure code

3. **Code Principles**
   - Favour immutability
   - Use data classes for simple data structures
   - Avoid side effects in functions
   - Prefer composition to inheritance
   - Use sealed classes for representing state
   - Use UUIDs for unique identifiers
   - Prefer objects to primitive types
   - Use idiomatic Kotlin code and minimize dependencies.
   - Avoid default values unless it helps write test data.

4. **Naming Conventions**
   - *Domain.kt for domain models
   - *Repository.kt for data access
   - *Service.kt for business logic
   - *Fake.kt for test doubles
   - *Client.kt for clients to other services

5. **Documentation**
   - Check ./doc directory for detailed guides on how to write tests
   - Keep README.md updated
   - Document complex business rules in code
   - Update .junie/guidelines.md with new learnings

6. **Exercises specific instructions**
   - A short introduction to the exercises can be found in ./doc/workshop/README.md
   - Comments on the tests should be considered as correct, verify them against the test code
   - Use the provided test cases as a guide for implementation
   - Ensure all tests pass
   - The workshop exericises have one Exercise[number]Test.kt which is a skeleton to work on. The implementation and example answer can be found in Exercise[number]TestAnswer.kt .

7. **Interactions**
   - Ask questions when clarification is needed
   - Ask when you need more information to perform a task
   - When refactoring, make sure to delete the old code
   - Run all tests after finishing a task
   - Compile often to get early feedback without running the tests
   - Run tests that you have changed frequently
