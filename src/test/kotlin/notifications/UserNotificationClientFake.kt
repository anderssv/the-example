package notifications

import java.io.IOException
import java.util.*

class UserNotificationClientFake : UserNotificationClient {
    private val notifications = mutableMapOf<String, MutableList<String>>()
    private val failingApplicationIds = mutableSetOf<UUID>()

    fun registerApplicationIdForFailure(applicationId: UUID) {
        failingApplicationIds.add(applicationId)
    }

    override fun notifyUser(applicationId: UUID, name: String, message: String) {
        if (failingApplicationIds.contains(applicationId)) {
            throw IOException("Simulated notification failure for application $applicationId")
        }
        notifications.getOrPut(name, ::mutableListOf).add(message)
    }

    /**
     * Only in the Fake
     *
     * Lets us verify that the user was indeed notified given a sequence of events in the system
     */
    fun getNotificationForUser(name: String): List<String> {
        return notifications.getOrDefault(name, emptyList())
    }
}
