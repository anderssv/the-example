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


sealed class RegistrationForm {
    data class InvalidAnonymousRegistrationForm(
        val email: Email, val anonymous: Boolean, val name: String?, val errors: List<String>
    ) : RegistrationForm()

    data class ValidAnonymousRegistrationForm(val email: Email.ValidEmail) : RegistrationForm()
    data class ValidRegistrationForm(val email: Email.ValidEmail, val name: String) : RegistrationForm()

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(email: Email, anonymous: Boolean, name: String?): RegistrationForm {
            return when (email) {
                is Email.InvalidEmail -> InvalidAnonymousRegistrationForm(email, anonymous, name, email.messages)
                is Email.ValidEmail -> if (anonymous && name == null) {
                    ValidAnonymousRegistrationForm(email)
                } else if (name != null && !anonymous) {
                    ValidRegistrationForm(email, name)
                } else {
                    InvalidAnonymousRegistrationForm(email, anonymous, name, listOf("Invalid combination"))
                }
            }
        }
    }
}

sealed class Response {
    data class OkResponse(val result: String) : Response()
    data class ErrorResponse(val errors: List<String>) : Response()
}

class ControllerLikeRegistrationController {
    fun registerUser(jsonString: String): Response {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(jsonString)

        return when (parsed) {
            is RegistrationForm.ValidRegistrationForm -> Response.OkResponse("Congrats ${parsed.name}!")
            is RegistrationForm.ValidAnonymousRegistrationForm -> Response.OkResponse("Congrats!")
            is RegistrationForm.InvalidAnonymousRegistrationForm -> Response.ErrorResponse(parsed.errors)
        }
    }
}