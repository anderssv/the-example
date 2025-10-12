package workshop

import application.Application
import application.ApplicationStatus
import application.valid
import customer.Customer
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Exercise 3—Manual DI, mocking and async testing
 *
 * Related Documentation:
 * - Manual Dependency Injection: doc/manual-dependency-injection.md
 * - System Design: doc/system-design.md
 *
 * Example Implementation:
 * - SystemTestContext.kt (test DI setup)
 * - SystemContext.kt (production DI setup)
 * - BrregClientTest.kt (example HTTP mocking)
 *
 * Answers:
 * - Exercise3TestAnswer.kt
 * - doc/workshop/exercise-answers.md
 */
class Exercise3Test {

    /**
     * Write a test that sets up all dependencies without the SystemContext.
     *
     * Questions:
     * - What is the benefit of using the SystemContext?
     * - What is the benefit of setting up all dependencies manually?
     * - How much should you set up manually?
     */
    @Test
    fun shouldStoreApplicationWithoutUsingSystemContext() {
        // TODO: Implement this test
        // 1. Set up all dependencies manually (applicationRepo, customerRepo, notificationClient, clock)
        // 2. Create a service with manual dependencies
        // 3. Set up test data (customer and application)
        // 4. Register the application using applicationService
        // 5. Verify that the application was stored correctly in the repository
    }

    /**
     * Write a test that verifies that the KTor client returns a null object when the remote responds with a 404.
     *
     * Questions:
     * - When would you use mocking instead of a fake implementation?
     * - Is it important to verify the request URL in this test?
     * - What are the trade-offs between mocking HTTP responses and using real HTTP calls in tests?
     */
    @Test
    fun testThatTheClientCodeBehavesAsExpectedOn404Responses() = runTest {
        // TODO: Implement this test
        // 1. Create a mock HttpClient that returns a 404 response
        // 2. Create a BrregClient with the mock engine
        // 3. Call the method being tested
        // 4. Verify the result is null
    }

    /**
     * Write a test that used delay to see interactions between async in Kotlin, delay and dispatchers.
     *
     * Questions:
     * - Do fakes have to be async?
     * - If you have to wait, can you avoid blocking?
     * - How parallel can you run tests?
     */
    @Test
    fun testSomethingAsync() = runTest {
        // TODO: Implement this test
        // 1. Define wait times (longWait and shortWait)
        // 2. Measure elapsed time for an async operation with delays
        // 3. Verify that elapsed time is within expected range
        // 4. Print wait times for comparison
    }

    /**
     * Just to have multiple tests that will wait to show that things are run in parallel.
     */
    @Test
    fun testSomethingAsync2() = runTest {
        // TODO: Implement this test
        // 1. Use a non-default dispatcher
        // 2. Add a delay to simulate waiting
    }
}
