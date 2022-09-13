class RegistrationService(private val registrationRepository: RegistrationRepository) {

    fun createNewRegistration(newRegistration: RegistrationForm.Valid.Registration) {
        registrationRepository.register(newRegistration)
    }

    fun createNewRegistration(newRegistration: RegistrationForm.Valid.AnonymousRegistration) {
        registrationRepository.register(newRegistration)
    }
}