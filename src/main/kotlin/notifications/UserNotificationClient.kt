package notifications

import java.util.*

interface UserNotificationClient {
    fun notifyUser(applicationId: UUID, name: String, message: String)
}

/**
 * Some kind of mail sender?
 */
class UserNotificationClientImpl : UserNotificationClient {

    override fun notifyUser(applicationId: UUID, name: String, message: String) {
        TODO("Not yet implemented")
    }

}
