# Claude Skills for The Example

This directory contains reusable Claude Code skills that teach Claude Code patterns and practices used in this project.

## Installation

Add these skills to your Claude Code installation:

```bash
npx skills add anderssv/the-example/skills
```

## Available Skills

### 🔀 kotlin-sum-types

Parse, don't validate - using sealed classes for type-safe validation and state representation.

**Location:** `domain/kotlin-sum-types/`

**What it teaches:**
- Sealed classes for representing valid/invalid states
- Parse don't validate principle
- Type-safe validation at boundaries
- Composable validation (Email → Address → RegistrationForm)
- Jackson integration with @JsonCreator
- Error collection with paths
- Making invalid states unrepresentable
- Controller pattern for handling valid/invalid cases

**Use when:**
- Parsing external input (JSON, CSV, user forms)
- Need to collect multiple validation errors
- Want type-safe guarantees about data validity
- Building domain models with complex validation
- Integrating with JSON parsing libraries

### 🧪 kotlin-tdd

Kotlin Test-Driven Development with fakes, object mothers, and Testing Through The Domain.

**Location:** `testing/kotlin-tdd/`

**What it teaches:**
- Three pillars: Test Setup, Fakes, and Testing Through The Domain (TTTD)
- Extension functions for test data with sensible defaults
- HashMap-based fakes instead of mocking frameworks
- SystemTestContext pattern with dependency injection
- TestClock for time control in tests
- Parallel-safe assertions for concurrent test execution
- Test tagging (unit, integration, database, e2e)
- When to use real implementations vs fakes
- When mocks are appropriate (HTTP protocol testing)

**Use when:**
- Writing Kotlin tests
- Setting up test infrastructure
- Need guidance on fakes vs mocks
- Testing time-dependent code
- Writing parallel-safe tests

### 🔌 kotlin-context-di

Manual dependency injection using SystemContext (production) and TestContext (test doubles) patterns.

**Location:** `practices/kotlin-context-di/`

**What it teaches:**
- SystemContext pattern for type-safe DI without frameworks
- TestContext pattern for test doubles
- Using interfaces for dependency grouping
- Anonymous objects for production wiring
- Typed test implementations to avoid casting
- Fresh context per test pattern
- Lazy vs direct initialization strategies
- Nullable-to-non-nullable narrowing in tests

**Use when:**
- Structuring service dependencies
- Wiring application components
- Creating test contexts
- Need alternative to framework-based DI
- Want full control over initialization

## Why These Skills?

These skills codify the patterns and practices demonstrated throughout this repository. They help Claude Code understand:
- How to write tests using the approaches shown in `/doc/tdd.md`, `/doc/fakes.md`, and `/doc/tttd.md`
- How to structure applications using the patterns in `/doc/manual-dependency-injection.md`
- The specific idioms and conventions used in this codebase

## Contributing

These skills are extracted from real working code in this repository. If you find improvements or want to add new skills, please open an issue or PR.
