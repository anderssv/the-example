---
name: kotlin-sum-types
description: Parse, don't validate - using sealed classes for type-safe validation and state representation. Model valid/invalid states explicitly, validate at boundaries, operate on valid types internally.
---

STARTER_CHARACTER = 🔀

# Parse, Don't Validate with Kotlin Sealed Classes

Represent validation states explicitly using sealed classes. This makes invalid states unrepresentable in your domain logic and pushes validation to system boundaries.

## Core Principle

**Parse, don't validate** means transforming untyped input into strongly-typed domain objects at the boundary, carrying proof of validity through the type system.

Instead of:
```kotlin
fun processEmail(email: String) {
    if (!isValid(email)) throw ValidationException()
    // Every function must revalidate or assume validity
}
```

Do this:
```kotlin
fun processEmail(email: ValidEmail) {
    // Type proves it's valid, no need to check
}
```

## Basic Pattern: Valid/Invalid States

Use sealed classes to represent parsed data that can be either valid or invalid:

```kotlin
sealed class Email {
    data class ValidEmail(
        val user: String,
        val domain: String,
    ) : Email() {
        fun stringRepresentation(): String = "$user@$domain"
    }

    data class InvalidEmail(
        val value: String,
        val _errors: List<ValidationError>,
    ) : Email(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> = _errors
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(createValue: String): Email =
            if (createValue.contains("@")) {
                createValue.split("@").let { ValidEmail(it.first(), it.last()) }
            } else {
                InvalidEmail(
                    createValue,
                    listOf(ValidationError("", "Not a valid Email", createValue))
                )
            }
    }
}
```

**Key elements:**
- Sealed class as parent (Email)
- ValidEmail with parsed structure (user, domain)
- InvalidEmail preserving original value + errors
- Static `create()` factory for parsing
- `@JsonCreator` for Jackson integration

## Validation Error Model

Standard error representation:

```kotlin
data class ValidationError(
    val path: String,      // Field path (e.g., "address.city")
    val message: String,   // Human-readable message
    val value: String,     // The invalid value
)

interface InvalidDataClass {
    fun hasErrors(): Boolean = getErrors().isNotEmpty()
    fun getErrors(): List<ValidationError>
}
```

All invalid states implement `InvalidDataClass` to expose errors uniformly.

## Composite Validation

Build complex types by composing validated types:

```kotlin
sealed class Address {
    data class ValidAddress(
        val streetName: String,
        val city: String,
        val postCode: String,
        val country: String,
    ) : Address()

    data class InvalidAddress(
        val streetName: String?,
        val city: String?,
        val postCode: String?,
        val country: String?,
        val _errors: List<ValidationError>,
    ) : Address(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> = _errors
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(
            streetName: String?,
            city: String?,
            postCode: String?,
            country: String?,
        ): Address {
            if (streetName.isNullOrEmpty() || city.isNullOrEmpty() ||
                postCode.isNullOrBlank() || country.isNullOrBlank()) {
                return InvalidAddress(
                    streetName, city, postCode, country,
                    listOf(ValidationError("", "Missing required fields", "..."))
                )
            }
            return ValidAddress(streetName, city, postCode, country)
        }
    }
}
```

**InvalidAddress preserves all input:** Even invalid data is kept so you can return meaningful error messages to users.

## Nested Valid States

Valid states can have their own hierarchy:

```kotlin
sealed class RegistrationForm {
    data class Invalid(
        val email: Email,
        val anonymous: Boolean,
        val name: String?,
        val address: Address?,
        val _errors: List<ValidationError>,
    ) : RegistrationForm(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> = _errors
    }

    sealed class Valid(
        open val email: Email.ValidEmail,  // Only ValidEmail allowed!
    ) : RegistrationForm() {
        data class AnonymousRegistration(
            val _email: Email.ValidEmail,
        ) : Valid(_email)

        data class Registration(
            val _email: Email.ValidEmail,
            val name: String,
            val address: Address.ValidAddress,  // Only ValidAddress allowed!
        ) : Valid(_email)
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(
            email: Email,
            anonymous: Boolean,
            name: String?,
            address: Address?,
        ): RegistrationForm {
            // Collect errors from nested validated types
            val errors = mapOf("email" to email, "address" to address)
                .filter { it.value is InvalidDataClass }
                .flatMap { (key, value) ->
                    (value as InvalidDataClass).getErrors()
                        .map { error ->
                            error.copy(
                                path = key + if (error.path.isNotEmpty()) ".${error.path}" else ""
                            )
                        }
                }

            return when {
                errors.isNotEmpty() -> Invalid(email, anonymous, name, address, errors)
                anonymous -> Valid.AnonymousRegistration(email as Email.ValidEmail)
                name != null -> Valid.Registration(
                    email as Email.ValidEmail,
                    name,
                    address as Address.ValidAddress
                )
                else -> Invalid(
                    email, anonymous, name, address,
                    listOf(ValidationError("", "Invalid combination", ""))
                )
            }
        }
    }
}
```

**Note the types:**
- `Valid.Registration` requires `Email.ValidEmail` and `Address.ValidAddress`
- Invalid case can contain any `Email` and `Address` (valid or invalid)
- Errors are propagated with paths ("email", "address.city")

## Controller Pattern

Handle valid/invalid cases at the boundary using `when`:

```kotlin
sealed class ControllerResponse {
    data class OkResponse(val result: String) : ControllerResponse()
    data class ErrorResponse(val errors: List<ValidationError>) : ControllerResponse()
}

class RegistrationController(
    private val registrationService: RegistrationService,
) {
    private val mapper = jacksonObjectMapper()

    fun registerUser(jsonString: String): ControllerResponse =
        when (val parsed: RegistrationForm = mapper.readValue(jsonString)) {
            is RegistrationForm.Valid -> {
                registrationService.createNewRegistration(parsed)
                when (parsed) {
                    is RegistrationForm.Valid.Registration ->
                        ControllerResponse.OkResponse("Congrats ${parsed.name}!")
                    is RegistrationForm.Valid.AnonymousRegistration ->
                        ControllerResponse.OkResponse("Congrats!")
                }
            }
            is RegistrationForm.Invalid ->
                ControllerResponse.ErrorResponse(parsed.getErrors())
        }
}
```

**Key aspects:**
- Parse JSON at the boundary
- `when` expression handles valid/invalid exhaustively
- Service receives only valid types
- Errors automatically collected and returned

## Jackson Integration

Use `@JsonCreator` to hook into Jackson parsing:

```kotlin
companion object {
    @JvmStatic
    @JsonCreator
    fun create(param1: Type1, param2: Type2): SealedClass {
        // Validation logic here
    }
}
```

Jackson calls `create()` during deserialization, giving you control over validation.

**Dependencies:**
```kotlin
implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
```

**Usage:**
```kotlin
val mapper = jacksonObjectMapper()
val parsed: RegistrationForm = mapper.readValue(jsonString)
// parsed is either Valid or Invalid
```

## Benefits

**Type Safety:**
- Invalid states are unrepresentable in domain logic
- Compiler prevents passing invalid data to functions expecting valid types
- `when` expressions ensure all cases are handled

**Error Collection:**
- Multiple validation errors collected in one pass
- Nested errors preserve field paths
- Original invalid values preserved for debugging

**Maintainability:**
- Validation logic centralized in `create()` factories
- Domain logic operates only on valid types
- Clear boundary between validated and unvalidated data

**Refactoring Safety:**
- Adding new fields to valid types is a compiler error if not handled
- Changing validation rules doesn't affect domain logic
- IDE autocomplete shows all valid/invalid states

## When to Use This Pattern

**Use sealed classes for validation when:**
- Parsing external input (JSON, CSV, user forms)
- Multiple fields must be validated together
- You need to collect multiple validation errors
- Invalid data should be preserved for error reporting
- Validation rules are complex or change frequently

**Don't use when:**
- Simple non-null checks (use Kotlin's `?` types)
- Single-field validation with no composition
- Data is already validated by external system (database constraints)
- Performance is critical (adds allocation overhead)

## Testing Strategy

**Test valid state creation:**
```kotlin
@Test
fun shouldParseValidEmail() {
    val parsed = Email.create("user@example.com")

    assertThat(parsed).isInstanceOf(Email.ValidEmail::class.java)
    (parsed as Email.ValidEmail).let {
        assertThat(it.user).isEqualTo("user")
        assertThat(it.domain).isEqualTo("example.com")
    }
}
```

**Test invalid state creation:**
```kotlin
@Test
fun shouldParseInvalidEmail() {
    val parsed = Email.create("not-an-email")

    assertThat(parsed).isInstanceOf(Email.InvalidEmail::class.java)
    (parsed as Email.InvalidEmail).let {
        assertThat(it.value).isEqualTo("not-an-email")
        assertThat(it.getErrors()).isNotEmpty()
    }
}
```

**Test composite validation:**
```kotlin
@Test
fun shouldCollectNestedErrors() {
    val form = RegistrationForm.create(
        email = Email.create("invalid"),
        anonymous = false,
        name = null,
        address = Address.create(null, null, null, null)
    )

    assertThat(form).isInstanceOf(RegistrationForm.Invalid::class.java)
    (form as RegistrationForm.Invalid).let {
        val errorPaths = it.getErrors().map { e -> e.path }
        assertThat(errorPaths).contains("email", "address")
    }
}
```

**Test controller handling:**
```kotlin
@Test
fun shouldReturnErrorResponseForInvalidInput() {
    val response = controller.registerUser("""{"email": "bad"}""")

    assertThat(response).isInstanceOf(ControllerResponse.ErrorResponse::class.java)
    (response as ControllerResponse.ErrorResponse).let {
        assertThat(it.errors).isNotEmpty()
    }
}
```

## Real-World Example

See complete working example:
- Domain: [RegistrationDomain.kt](../../../src/main/kotlin/user/registration/RegistrationDomain.kt)
- Controller: [ControllerLikeRegistrationController.kt](../../../src/main/kotlin/user/registration/ControllerLikeRegistrationController.kt)
- Tests: [ParsingTest.kt](../../../src/test/kotlin/user/registration/ParsingTest.kt)

## Anti-Patterns

**Don't validate in the domain:**
```kotlin
// BAD - validation scattered throughout domain
fun processRegistration(email: String, name: String) {
    require(email.contains("@")) { "Invalid email" }
    require(name.isNotBlank()) { "Name required" }
    // ... business logic
}
```

Do validation at boundaries, pass validated types to domain:
```kotlin
// GOOD - validation at boundary, domain receives valid types
fun processRegistration(registration: RegistrationForm.Valid.Registration) {
    // registration.email is ValidEmail, no validation needed
}
```

**Don't lose original invalid values:**
```kotlin
// BAD - can't tell user what they sent
data class InvalidEmail(val _errors: List<ValidationError>) : Email()
```

Keep the original value:
```kotlin
// GOOD - can show user what they sent
data class InvalidEmail(
    val value: String,  // Preserve original input
    val _errors: List<ValidationError>
) : Email()
```

**Don't use exceptions for expected validation:**
```kotlin
// BAD - exceptions for expected invalid input
fun create(email: String): ValidEmail {
    if (!isValid(email)) throw ValidationException()
    return ValidEmail(...)
}
```

Return sealed class representing valid/invalid:
```kotlin
// GOOD - invalid input is expected, not exceptional
fun create(email: String): Email {  // Returns Valid or Invalid
    if (!isValid(email)) return InvalidEmail(...)
    return ValidEmail(...)
}
```

## Related Patterns

- **Railway-Oriented Programming**: Result<T, E> types (similar but more functional)
- **Type-Driven Development**: Using types to guide design
- **Domain-Driven Design**: Validation at aggregate boundaries

## References

- [Parse, don't validate](https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/) by Alexis King
- [Making Illegal States Unrepresentable](https://fsharpforfunandprofit.com/posts/designing-with-types-making-illegal-states-unrepresentable/) by Scott Wlaschin
- [Type-Driven Development talk at JavaZone 2022](https://2022.javazone.no/#/program/54a82962-e8f4-424c-864a-e1f987825dc7)
