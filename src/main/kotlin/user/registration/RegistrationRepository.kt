package user.registration

class RegistrationRepository {
    private val db = mutableMapOf<Email.ValidEmail, RegistrationForm.Valid>()

    fun register(newRegistration: RegistrationForm.Valid) {
        db[newRegistration.email] = newRegistration
        // Don't do this kind string manipulation. It is just added as an example to get close to real
        // life on how to access the type system.
        when (newRegistration) {
            is RegistrationForm.Valid.Registration -> "INSERT INTO registrations(email, name, anonymous) VALUES ('${newRegistration.email.stringRepresentation()}', '${newRegistration.name}', 'false' )"
            is RegistrationForm.Valid.AnonymousRegistration -> "INSERT INTO registrations(email, anonymous) VALUES ('${newRegistration.email.stringRepresentation()}, 'true' )"
        }
    }

    fun getRegistration(email: Email.ValidEmail): RegistrationForm.Valid? {
        return db[email]
    }
}