package user.registration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

sealed class ControllerResponse {
    data class OkResponse(val result: String) : ControllerResponse()
    data class ErrorResponse(val errors: List<ValidationError>) : ControllerResponse()
}

class ControllerLikeRegistrationController(private val registrationService: RegistrationService) {
    private val mapper = jacksonObjectMapper()
    fun registerUser(jsonString: String): ControllerResponse {
        return when (val parsed: RegistrationForm = mapper.readValue(jsonString)) {
            // Doing two levels here like the classes are wrapped, but you can
            // actually do all three on the "same level".
            is RegistrationForm.Valid -> {
                registrationService.createNewRegistration(parsed)
                when (parsed) {
                    is RegistrationForm.Valid.Registration -> {
                        ControllerResponse.OkResponse("Congrats ${parsed.name}!")
                    }

                    is RegistrationForm.Valid.AnonymousRegistration -> {
                        ControllerResponse.OkResponse("Congrats!")
                    }
                }
            }

            is RegistrationForm.Invalid -> ControllerResponse.ErrorResponse(parsed.getErrors())
        }
    }
}