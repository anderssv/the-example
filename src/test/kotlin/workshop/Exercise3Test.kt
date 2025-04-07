package workshop

import org.junit.jupiter.api.Test

/**
 * Exercise 3—Manual DI, mocking and async testing
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
    fun testThatTheClientCodeBehavesAsExpectedOn404Responses() {
        // TODO: Implement this test
        // 1. Create a mock HttpClient that returns a 404 response
        // 2. Create a BrregClient with the mock engine
        // 3. Call the method being tested
        // 4. Verify the result is null
    }
}