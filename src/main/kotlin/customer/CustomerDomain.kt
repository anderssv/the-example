package customer

import java.util.*

data class Customer(
    val id: UUID,
    val name: String,
    val active: Boolean,
) {
    companion object
}
