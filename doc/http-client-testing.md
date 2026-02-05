Part of [TDD](tdd.md)

---

> I am an independent consultant and would love to help your team get better at continuous delivery.
> Reach out
> at [anders.sveen@mikill.no](mailto:anders.sveen@mikill.no) or go
> to [https://www.mikill.no](https://www.mikill.no/contact.html) to contact, follow on social media or to see more of
> my work.

When testing HTTP clients, you have a choice: use a [Fake](fakes.md) or use a mock HTTP engine.
This is one of the few places where I actually recommend mocks over fakes.

# When to use MockEngine vs Fakes

**Use Fakes** (the default) when:
- Testing business logic that uses the client
- Testing component interactions
- Speed matters (fakes are faster)

**Use MockEngine** when:
- Testing specific HTTP status codes (404, 500, 503)
- Testing response parsing and deserialization
- Verifying request construction (URL, headers, body)
- Testing timeout and retry behavior
- Testing error handling for protocol-level issues

The key insight: MockEngine tests the *adapter implementation*, while fakes test the *system that uses the adapter*.

# Example: Testing with Ktor's MockEngine

See [BrregClientTest.kt](../src/test/kotlin/brreg/BrregClientTest.kt) for complete examples.

## Testing successful responses

```kotlin
@Test
fun shouldFetchEntityByOrganizationNumber() = runTest {
    val mockEngine = MockEngine { request ->
        // Verify the request is correct
        assertThat(request.url.toString())
            .isEqualTo("https://api.example.com/entities/123")

        // Return a mock response
        respond(
            content = """{"id": "123", "name": "Test Entity"}""",
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type" to listOf("application/json"))
        )
    }

    val client = MyClientImpl(MyClientImpl.client(mockEngine))
    val entity = client.getEntity("123")

    assertThat(entity).isNotNull
    assertThat(entity.name).isEqualTo("Test Entity")
}
```

## Testing 404 responses

```kotlin
@Test
fun shouldReturnNullWhenNotFound() = runTest {
    val mockEngine = MockEngine { request ->
        respond(
            content = "",
            status = HttpStatusCode.NotFound
        )
    }

    val client = MyClientImpl(MyClientImpl.client(mockEngine))
    val entity = client.getEntity("missing")

    assertThat(entity).isNull()
}
```

## Testing error handling

```kotlin
@Test
fun shouldThrowExceptionOnNetworkError() = runTest {
    val mockEngine = MockEngine { _ ->
        throw IOException("Connection refused")
    }

    val client = MyClientImpl(MyClientImpl.client(mockEngine))

    assertThrows<Exception> {
        client.getEntity("123")
    }
}
```

# Client design for testability

To make your HTTP clients testable with MockEngine, design them to accept an engine:

```kotlin
class MyClientImpl(
    private val httpClient: HttpClient = client()
) : MyClient {

    companion object {
        fun client(engine: HttpClientEngine = CIO.create()): HttpClient =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json()
                }
            }
    }

    override suspend fun getEntity(id: String): Entity? {
        // Implementation using httpClient
    }
}
```

This allows tests to inject `MockEngine` while production uses the default CIO engine.

# When NOT to use MockEngine

Don't use MockEngine for:
- Testing business logic (use fakes for the client interface instead)
- Integration tests that should hit real APIs (use `@Tag("integration")` for these)
- Testing retry logic across multiple components (test at the right level)

Remember: MockEngine tests the HTTP layer. If you're testing business rules, you should be using a fake implementation of your client interface, not mocking HTTP.

# Real API integration tests

For critical integrations, also include a test against the real API:

```kotlin
@Test
@Tag("integration")
fun shouldFetchEntityFromRealApi() = runTest {
    val client = MyClientImpl()  // Uses real HTTP engine

    val entity = client.getEntity("known-id")

    assertThat(entity).isNotNull
}
```

Tag these tests so they can be excluded from fast local test runs but included in CI pipelines.

# Related reading

- [Fakes](fakes.md) - Default approach for test doubles
- [Ktor MockEngine documentation](https://ktor.io/docs/client-testing.html)
- [BrregClientTest.kt](../src/test/kotlin/brreg/BrregClientTest.kt) - Full working example
