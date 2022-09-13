class RegistrationService(private val registrationRepository: RegistrationRepository) {

    fun createNewRegistration(newRegistration: RegistrationForm.ValidRegistrationForm) {
        registrationRepository.register(newRegistration)
    }

    fun createNewRegistration(newRegistration: RegistrationForm.ValidAnonymousRegistrationForm) {
        registrationRepository.register(newRegistration)
    }
}