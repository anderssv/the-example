class RegistrationService(private val registrationRepository: RegistrationRepository) {

    fun createNewRegistration(newRegistration: RegistrationForm.Valid) {
        registrationRepository.register(newRegistration)
    }

}