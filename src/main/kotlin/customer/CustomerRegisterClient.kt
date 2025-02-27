package customer


interface CustomerRegisterClient {
    fun addCustomer(customer: Customer)
    fun getCustomer(name: String): Customer
}

class CustomerRegisterClientImpl : CustomerRegisterClient {
    override fun addCustomer(customer: Customer) {
        TODO("Not yet implemented")
    }

    override fun getCustomer(name: String): Customer {
        TODO("Not yet implemented")
    }
}