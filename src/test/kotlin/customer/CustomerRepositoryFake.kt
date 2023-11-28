package customer;

class CustomerRepositoryFake: CustomerRepository {
    private val db = mutableMapOf<String, Customer>()

    override fun addCustomer(customer: Customer) {
        db[customer.name] = customer
    }

    override fun getCustomer(name: String): Customer {
        return db[name]!!
    }
}
