package notifications

class UserNotificationClientFake : UserNotificationClient {
    private val notifications = mutableMapOf<String, MutableList<String>>()

    override fun notifyUser(name: String, message: String) {
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
