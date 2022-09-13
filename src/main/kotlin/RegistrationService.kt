class RegistrationService(private val registrationRepository: RegistrationRepository) {

    fun createNewRegistration(newRegistration: RegistrationForm.Valid) {
        if (isAllowedToRegister(newRegistration)) registrationRepository.register(newRegistration)
    }

    private fun isAllowedToRegister(newRegistration: RegistrationForm.Valid): Boolean {
        return newRegistration.email.domain.endsWith(".com") && when (newRegistration) {
            is RegistrationForm.Valid.Registration -> !newRegistration.name.contains("fuck")
            is RegistrationForm.Valid.AnonymousRegistration -> true
        }
    }

}