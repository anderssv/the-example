import com.fasterxml.jackson.annotation.JsonCreator

data class ValidationError(val path: String, val message: String, val value: String)

interface InvalidDataClass {
    fun hasErrors(): Boolean = getErrors().isNotEmpty()
    fun getErrors(): List<ValidationError>
}

sealed class Email {
    data class ValidEmail(val user: String, val domain: String) : Email()
    data class InvalidEmail(val value: String, val _errors: List<ValidationError>) : Email(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> {
            return _errors
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
        val _errors: List<ValidationError>
    ) : Address(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> {
            return this._errors
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
    data class Invalid(
        val email: Email,
        val anonymous: Boolean,
        val name: String?,
        val address: Address?,
        val _errors: List<ValidationError>
    ) : RegistrationForm(), InvalidDataClass {
        override fun getErrors(): List<ValidationError> {
            return _errors
        }
    }

    sealed class Valid: RegistrationForm() {
        data class AnonymousRegistration(val email: Email.ValidEmail) : Valid()
        data class Registration(val email: Email.ValidEmail, val name: String, val address: Address.ValidAddress) :
            Valid()
    }

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
                Invalid(email, anonymous, name, address, errors)
            } else if (anonymous) {
                Valid.AnonymousRegistration(email as Email.ValidEmail)
            } else if (name != null) {
                Valid.Registration(email as Email.ValidEmail, name, address as Address.ValidAddress)
            } else {
                Invalid(
                    email, anonymous, name, address, listOf(ValidationError("", "Invalid combination!", "someValue"))
                )
            }
        }
    }
}
