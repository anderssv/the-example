package application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import system.SystemTestContext
import java.time.Duration
import java.time.LocalDate

class ApplicationExpirationTest {
    private val testContext = SystemTestContext()

    @Test
    fun shouldRegisterApplicationSuccessfullyAndExpireWhenTooOld() {
        with(testContext) {
            // Set initial date and create application using current time
            clock.setTo(LocalDate.of(2022, 1, 1))
            val application = Application.valid(applicationDate = LocalDate.now(clock))
            applicationService.registerInitialApplication(application)
            
            // Verify it's active
            assertThat(repositories.applicationRepo.getApplication(application.id).status)
                .isEqualTo(ApplicationStatus.ACTIVE)
            
            // Move time forward 7 months
            clock.advance(Duration.ofDays(7 * 30))
            
            // Run expiration check
            applicationService.expireApplications()
            
            // Verify the application is now expired
            assertThat(repositories.applicationRepo.getApplication(application.id).status)
                .isEqualTo(ApplicationStatus.EXPIRED)
        }
    }
}