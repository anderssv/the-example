package customer


interface CustomerRepository {
    fun addCustomer(customer: Customer)
    fun getCustomer(name: String): Customer
}

class CustomerRepositoryImpl : CustomerRepository {
    override fun addCustomer(customer: Customer) {
        TODO("Not yet implemented")
    }

    override fun getCustomer(name: String): Customer {
        TODO("Not yet implemented")
    }

}