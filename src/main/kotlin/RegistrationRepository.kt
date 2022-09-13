class RegistrationRepository {
    private val db = mutableMapOf<Email.ValidEmail, RegistrationForm.Valid>()

    fun register(newRegistration: RegistrationForm.Valid) {
        // Could possibly have two different INSERT statements here
        when (newRegistration) {
            is RegistrationForm.Valid.Registration -> db[newRegistration.email] = newRegistration
            is RegistrationForm.Valid.AnonymousRegistration -> db[newRegistration.email] = newRegistration
        }
    }

    fun getRegistration(email: Email.ValidEmail): RegistrationForm.Valid? {
        return db[email]
    }
}