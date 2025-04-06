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
}