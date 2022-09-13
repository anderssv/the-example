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
            is RegistrationForm.Valid.ValidRegistrationForm -> {
                registrationService.createNewRegistration(parsed)
                ControllerResponse.OkResponse("Congrats ${parsed.name}!")
            }
            is RegistrationForm.Valid.ValidAnonymousRegistrationForm -> {
                registrationService.createNewRegistration(parsed)
                ControllerResponse.OkResponse("Congrats!")
            }
            is RegistrationForm.InvalidAnonymousRegistrationForm -> ControllerResponse.ErrorResponse(parsed.getErrors())
        }
    }
}