import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

sealed class Email {
    data class ValidEmail(val user: String, val domain: String) : Email()
    data class InvalidEmail(val value: String, val messages: List<String>) : Email()

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(createValue: String): Email {
            return if (createValue.contains("@")) {
                createValue.split("@").let { ValidEmail(it.first(), it.last()) }
            } else {
                InvalidEmail(createValue, listOf("Not a valid Email ;)"))
            }
        }
    }


}

data class RegistrationForm(val email: Email, val anonymous: Boolean, val name: String)

sealed class Response {
    data class OkResponse(val result: String) : Response()
    data class ErrorResponse(val errors: List<String>) : Response()
}

class ControllerLikeRegistrationController {
    fun registerUser(jsonString: String): Response {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(jsonString)

        return when (parsed.email) {
            is Email.ValidEmail -> Response.OkResponse("Congrats!")
            is Email.InvalidEmail -> Response.ErrorResponse(parsed.email.messages)
        }
    }
}