package workshop

import org.junit.jupiter.api.Test
import system.SystemTestContext

/**
 * Exercise 1 - Bootup, test data and arrange-assert-act
 *
 * We use the testContext to get access to the system under test.
 * We will dive into the setup of the testContext in a later exercise.
 */
class Exercise1Test {
    private val testContext = SystemTestContext()

    /**
     * Write a test that registers an application and verifies that it was stored correctly.
     *
     * Hint: Use the applicationService to register the application and the applicationRepo to verify that it was stored correctly.
     *
     * Questions:
     * - How do we set up and re-use test data?
     * - What is the best abstraction to verify that the application was stored correctly?
     */
    @Test
    fun shouldRegisterApplicationAndStoreCorrectly() {
        with(testContext) {
            // TODO: Implement this test
            // 1. Set up test data (customer and application)
            // 2. Register the application using applicationService
            // 3. Verify that the application was stored correctly in the repository
        }
    }

    /**
     * Write a test that registers and application that is older than 6 months, expires it and verifies that it was expired.
     *
     * Questions:
     * - How can we re-use test data setup and make in tunable in each test?
     * - How do we signify what are the important changes for that test?
     * - Should we use data on the application to verify or ask questions?
     */
    @Test
    fun shouldRegisterAndApplicationAndModifyForTesting() {
        with(testContext) {
            // TODO: Implement this test
            // 1. Set up test data with an application older than 6 months
            // 2. Register the application
            // 3. Expire the application through domain logic in applicationService
            // 4. Verify the application is expired in the repository
        }
    }

    /**
     * Write a test that verifies that the service throws an IllegalStateException when trying to approve a DENIED application.
     *
     * Questions:
     * - Is this a normal control flow or an exceptional case?
     * - What solutions are alternatives here?
     * - Can or should we encode the arrange, act assert steps?
     */
    @Test
    fun shouldThrowExceptionWhenApprovingDeniedApplication() {
        with(testContext) {
            // TODO: Implement this test
            // 1. Set up a test data application with DENIED status and store it in the system
            // 2. Verify that attempting to approve throws IllegalStateException
        }
    }
}
