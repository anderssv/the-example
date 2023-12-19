package fakes

import notifications.UserNotificationClient

class UserNotificationClientFake : UserNotificationClient {
    private val notifications = mutableMapOf<String, MutableList<String>>()

    /**
     * Only in the Fake
     *
     * Lets us verify that the user was indeed notified given a sequence of events in the system
     */
    fun getNotificationForUser(name: String): List<String> {
        return notifications.getOrDefault(name, emptyList())
    }

    override fun notifyUser(name: String, message: String) {
        notifications.getOrPut(name, ::mutableListOf).add(message)
    }

}
