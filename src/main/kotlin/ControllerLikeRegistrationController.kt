import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

sealed class ControllerResponse {
    data class OkResponse(val result: String) : ControllerResponse()
    data class ErrorResponse(val errors: List<ValidationError>) : ControllerResponse()
}

class ControllerLikeRegistrationController(val registrationService: RegistrationService) {
    fun registerUser(jsonString: String): ControllerResponse {
        val mapper = jacksonObjectMapper()

        return when (val parsed: RegistrationForm = mapper.readValue(jsonString)) {
            // Doing two levels here like the classes are wrapped, but you can
            // actually do all three on the "same level".
            is RegistrationForm.Valid -> {
                when (parsed) {
                    is RegistrationForm.Valid.Registration -> {
                        registrationService.createNewRegistration(parsed)
                        ControllerResponse.OkResponse("Congrats ${parsed.name}!")
                    }

                    is RegistrationForm.Valid.AnonymousRegistration -> {
                        registrationService.createNewRegistration(parsed)
                        ControllerResponse.OkResponse("Congrats!")
                    }
                }
            }

            is RegistrationForm.Invalid -> ControllerResponse.ErrorResponse(parsed.getErrors())
        }
    }
}