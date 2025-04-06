package workshop

import application.*
import brreg.BrregClient
import brreg.BrregClientImpl
import brreg.BrregEntity
import customer.Customer
import customer.CustomerRegisterClientFake
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import notifications.UserNotificationClientFake
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Exercise 3—Manual DI, mocking and async testing
 */
class Exercise3TestAnswer {

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
        // Arrange: Set up all dependencies manually
        val applicationRepo = ApplicationRepositoryFake()
        val customerRepo = CustomerRegisterClientFake()
        val notificationClient = UserNotificationClientFake()
        val fixedClock = Clock.fixed(
            Instant.parse("2023-01-01T10:00:00Z"),
            ZoneId.systemDefault()
        )

        // Create a service with manual dependencies
        val applicationService = ApplicationService(
            applicationRepo = applicationRepo,
            customerRepository = customerRepo,
            userNotificationClient = notificationClient,
            clock = fixedClock
        )

        // Arrange: Set up test data
        val customDate = LocalDate.of(2023, 1, 1)
        val customer = Customer.valid()
        val application = Application.valid(applicationDate = customDate, customerId = customer.id)
            .copy(name = "Manual Setup Test")

        // Act: Perform the action being tested
        applicationService.registerInitialApplication(customer, application)

        // Assert: Verify the expected outcome
        val storedApplication = applicationRepo.getApplication(application.id)
        assertThat(storedApplication.status).isEqualTo(ApplicationStatus.ACTIVE)
        assertThat(storedApplication.applicationDate).isEqualTo(customDate)
        assertThat(storedApplication.name).isEqualTo("Manual Setup Test")
    }

    /**
     * This test demonstrates that mocking is useful when we need to verify
     * client interactions based on response codes.
     * 
     * It shows how we can:
     * 1. Control the response code returned by the mock
     * 2. Verify that the client handles the response code correctly
     * 3. Verify that the client makes the correct request
     */
    @Test
    fun shouldReturnNullWhenEntityNotFound() = runTest {
        // Arrange: Create a mock HttpClient that returns a 404 response
        val mockEngine = MockEngine { request ->
            // Verify the request URL
            assertThat(request.url.toString()).isEqualTo("https://data.brreg.no/enhetsregisteret/api/enheter/999999999")

            // Return a 404 response
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }

        // Create a BrregClient with the mock engine
        val brregClient: BrregClient = BrregClientImpl(BrregClientImpl.client(mockEngine))

        // Act: Call the method being tested
        val entity = brregClient.getEntity("999999999")

        // Assert: Verify the result
        assertThat(entity).isNull()
    }
}
