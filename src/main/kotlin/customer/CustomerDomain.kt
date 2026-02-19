@file:Suppress("ktlint:standard:filename")

package customer

import java.util.UUID

data class Customer(
    val id: UUID,
    val name: String,
    val active: Boolean,
) {
    companion object
}
