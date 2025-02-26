# Claude's Guide for The Example Project

## Build/Test Commands
```
./gradlew build                 # Full build with tests
./gradlew test                  # Run all tests
./gradlew test --tests "workshop.Exercise2Test" # Run specific test class
./gradlew test --tests "*.shouldDemonstrateNotificationVerification" # Run specific test method
```

## Code Style Guidelines
- **Indentation**: 4 spaces
- **Naming**: CamelCase for classes/methods/properties
- **File Conventions**: *Domain.kt, *Repository.kt, *Service.kt, *Fake.kt, *Client.kt
- **Architecture**: Manual DI via SystemContext, Repository pattern, Rich domain models
- **Types**: Use sealed classes for domain modeling with Valid/Invalid variants

## Best Practices
- **Immutability**: Favor immutable data structures and pure functions
- **Error Handling**: Domain validation via sealed classes, explicit exceptions for exceptional cases
- **TDD Approach**: Write tests first, follow Arrange-Act-Assert pattern
- **Testing**: Use fakes instead of mocks, DSLs for test setup, SystemTestContext for dependency management

## Tech Stack
- Kotlin JVM (Java 21)
- Gradle with Kotlin DSL
- JUnit and AssertJ
- Jackson for JSON handling

See /doc directory for detailed architectural documentation.