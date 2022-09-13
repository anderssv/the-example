class RegistrationRepository {
    private val db = mutableMapOf<Email.ValidEmail, RegistrationForm.Valid>()

    fun register(newRegistration: RegistrationForm.Valid.ValidRegistrationForm) {
        db[newRegistration.email] = newRegistration
    }

    fun register(newRegistration: RegistrationForm.Valid.ValidAnonymousRegistrationForm) {
        db[newRegistration.email] = newRegistration
    }

    fun getRegistration(email: Email.ValidEmail): RegistrationForm.Valid? {
        return db[email]
    }
}