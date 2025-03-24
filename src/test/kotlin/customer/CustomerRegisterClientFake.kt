package customer

import java.util.*

class CustomerRegisterClientFake: CustomerRegisterClient {
    private val customersById = mutableMapOf<UUID, Customer>()
    private val customersByName = mutableMapOf<String, Customer>()

    override fun addCustomer(customer: Customer) {
        customersById[customer.id] = customer
        customersByName[customer.name] = customer
    }

    override fun getCustomer(id: UUID): Customer? {
        return customersById[id]
    }

}
