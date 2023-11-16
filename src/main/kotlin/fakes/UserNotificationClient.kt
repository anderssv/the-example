package fakes

interface UserNotificationClient {
    fun notifyUser(name: String, message: String)
}

/**
 * Some kind of mail sender?
 */
class UserNotificationClientImpl : UserNotificationClient {
    override fun notifyUser(name: String, message: String) {
        TODO("Not yet implemented")
    }

}
