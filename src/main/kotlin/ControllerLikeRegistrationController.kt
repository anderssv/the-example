import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class ValidationError(val path: String, val message: String, val value: String)

interface InvalidDataClass {
    fun hasErrors(): Boolean = getErrors().isNotEmpty()
    fun getErrors(): List<ValidationError>
}

sealed class Email {
    data class ValidEmail(val user: String, val domain: String) : Email()
    data class InvalidEmail(val value: String, val messages: List<ValidationError>) : Email(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> {
            return messages
        }
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(createValue: String): Email {
            return if (createValue.contains("@")) {
                createValue.split("@").let { ValidEmail(it.first(), it.last()) }
            } else {
                InvalidEmail(createValue, listOf(ValidationError("", "Not a valid Email ;)", createValue)))
            }
        }
    }
}

sealed class Address {
    data class ValidAddress(val streetName: String, val city: String, val poCode: String, val country: String) :
        Address()

    data class InvalidAddress(
        val streetName: String?,
        val city: String?,
        val poCode: String?,
        val country: String?,
        val messages: List<ValidationError>
    ) : Address(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> {
            return this.messages
        }
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(streetName: String?, city: String?, poCode: String?, country: String?): Address {
            return if (streetName.isNullOrEmpty() || city.isNullOrEmpty() || poCode.isNullOrBlank() || country.isNullOrBlank()) {
                InvalidAddress(
                    streetName, city, poCode, country, listOf(
                        ValidationError(
                            "", "Missing values", listOf(streetName, city, poCode, country).joinToString(":")
                        )
                    )
                )
            } else {
                ValidAddress(streetName, city, poCode, country)
            }
        }
    }

}

sealed class RegistrationForm {
    data class InvalidAnonymousRegistrationForm(
        val email: Email,
        val anonymous: Boolean,
        val name: String?,
        val address: Address?,
        val messages: List<ValidationError>
    ) : RegistrationForm(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> {
            return messages
        }
    }

    data class ValidAnonymousRegistrationForm(val email: Email.ValidEmail) : RegistrationForm()
    data class ValidRegistrationForm(val email: Email.ValidEmail, val name: String, val address: Address.ValidAddress) :
        RegistrationForm()

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(email: Email, anonymous: Boolean, name: String?, address: Address?): RegistrationForm {
            // How can we rely less on typing here? Maybe the mapOf part is good enough...
            val errors = mapOf("email" to email, "address" to address).filter {
                it.value is InvalidDataClass
            }.map {
                (it.value as InvalidDataClass).let { dataClass ->
                    dataClass.getErrors()
                        .map { error -> error.copy(path = it.key + if (error.path.isNotEmpty()) ".${error.path}" else "") }
                }
            }.flatten()

            return if (errors.isNotEmpty()) {
                InvalidAnonymousRegistrationForm(email, anonymous, name, address, errors)
            } else if (anonymous) {
                ValidAnonymousRegistrationForm(email as Email.ValidEmail)
            } else if (name != null) {
                ValidRegistrationForm(email as Email.ValidEmail, name, address as Address.ValidAddress)
            } else {
                InvalidAnonymousRegistrationForm(
                    email, anonymous, name, address, listOf(ValidationError("", "Invalid combination!", "someValue"))
                )
            }
        }
    }
}

sealed class Response {
    data class OkResponse(val result: String) : Response()
    data class ErrorResponse(val errors: List<ValidationError>) : Response()
}

class ControllerLikeRegistrationController {
    fun registerUser(jsonString: String): Response {
        val mapper = jacksonObjectMapper()

        return when (val parsed: RegistrationForm = mapper.readValue(jsonString)) {
            is RegistrationForm.ValidRegistrationForm -> Response.OkResponse("Congrats ${parsed.name}!")
            is RegistrationForm.ValidAnonymousRegistrationForm -> Response.OkResponse("Congrats!")
            is RegistrationForm.InvalidAnonymousRegistrationForm -> Response.ErrorResponse(parsed.messages)
        }
    }
}