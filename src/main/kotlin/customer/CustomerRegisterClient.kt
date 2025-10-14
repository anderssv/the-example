package customer

import java.util.*

interface CustomerRegisterClient {
    fun addCustomer(customer: Customer)

    fun getCustomer(id: UUID): Customer?
}

class CustomerRegisterClientImpl : CustomerRegisterClient {
    override fun addCustomer(customer: Customer) {
        TODO("Not yet implemented")
    }

    override fun getCustomer(id: UUID): Customer {
        TODO("Not yet implemented")
    }
}
