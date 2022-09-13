class RegistrationService(private val registrationRepository: RegistrationRepository) {

    fun createNewRegistration(newRegistration: RegistrationForm.Valid.ValidRegistrationForm) {
        registrationRepository.register(newRegistration)
    }

    fun createNewRegistration(newRegistration: RegistrationForm.Valid.ValidAnonymousRegistrationForm) {
        registrationRepository.register(newRegistration)
    }
}