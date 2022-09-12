import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertAll
import kotlin.test.Test

class ParsingTest {

    @Test
    fun testShouldParseInvalidEmail() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(getTestJson("hello"))

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.InvalidAnonymousRegistrationForm::class.java)
        (parsed as RegistrationForm.InvalidAnonymousRegistrationForm).let {
            assertThat(it.email).isExactlyInstanceOf(Email.InvalidEmail::class.java)
            (it.email as Email.InvalidEmail).let {
                assertThat(it.value).isEqualTo("hello")
            }
        }
    }

    @Test
    fun testShouldParseMissingAddress() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(getTestJson("hello", addressJson = null))

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.InvalidAnonymousRegistrationForm::class.java)
        (parsed as RegistrationForm.InvalidAnonymousRegistrationForm).let {
            assertThat(it.email).isExactlyInstanceOf(Email.InvalidEmail::class.java)
            (it.email as Email.InvalidEmail).let {
                assertThat(it.value).isEqualTo("hello")
            }
            assertThat(it.address).isNull()
        }
    }

    @Test
    fun testShouldParseValidAddress() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(getTestJson("hello"))

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.InvalidAnonymousRegistrationForm::class.java)
        (parsed as RegistrationForm.InvalidAnonymousRegistrationForm).let {
            assertThat(it.email).isExactlyInstanceOf(Email.InvalidEmail::class.java)
            (it.email as Email.InvalidEmail).let {
                assertThat(it.value).isEqualTo("hello")
            }
            assertThat(it.address).isNotNull
            assertThat(it.address).isExactlyInstanceOf(Address.ValidAddress::class.java)
            assertThat((it.address as Address.ValidAddress).city).isEqualTo("Oslo")
        }
    }

    @Test
    fun testShouldParseInvalidAddress() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(
            getTestJson(
                "hello", """
                  "address": { "city": "Oslo" }
                """.trimIndent()
            )
        )

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.InvalidAnonymousRegistrationForm::class.java)
        (parsed as RegistrationForm.InvalidAnonymousRegistrationForm).let {
            assertThat(it.email).isExactlyInstanceOf(Email.InvalidEmail::class.java)
            (it.email as Email.InvalidEmail).let {
                assertThat(it.value).isEqualTo("hello")
            }
            assertThat(it.address).isNotNull
            assertThat(it.address).isExactlyInstanceOf(Address.InvalidAddress::class.java)
            assertThat((it.address as Address.InvalidAddress).city).isEqualTo("Oslo")
        }
    }

    @Test
    fun testShouldParseValidEmail() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(getTestJson("hello@hello.com"))

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.ValidRegistrationForm::class.java)
        (parsed as RegistrationForm.ValidRegistrationForm).let {
            assertAll({ assertThat(it.email.user).isEqualTo("hello") },
                { assertThat(it.email.domain).isEqualTo("hello.com") })
        }
    }

    @Test
    fun testShouldFailWhenInvalidEmailInController() {
        val controller = ControllerLikeRegistrationController()
        val result = controller.registerUser(getTestJson("invalid-email"))

        assertThat(result).isExactlyInstanceOf(Response.ErrorResponse::class.java)
        (result as Response.ErrorResponse).let {
            assertThat(it.errors).isNotEmpty
            assertThat(it.errors).contains(ValidationError("email", "Not a valid Email ;)", "invalid-email"))
        }
    }

    private fun getTestJson(email: String, addressJson: String? = getAddressJson()) = """
                {
                    "email": "$email",
                    ${if (addressJson != null) "$addressJson," else ""}
                    "anonymous": false,
                    "name": "Myname"   
                }
            """.trimIndent()

    private fun getAddressJson(): String {
        return """
            "address": {
                "streetName": "test",
                "city": "Oslo",
                "poCode": "0164",
                "country": "Norway"
            }
        """.trimIndent()
    }
}