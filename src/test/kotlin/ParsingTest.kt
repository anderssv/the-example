import Email.ValidEmail
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

        assertThat(parsed.email).isExactlyInstanceOf(Email.InvalidEmail::class.java)
        (parsed.email as Email.InvalidEmail).let {
            assertThat(it.value).isEqualTo("hello")
        }
    }

    @Test
    fun testShouldParseValidEmail() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(getTestJson("hello@hello.com"))

        assertThat(parsed.email).isExactlyInstanceOf(ValidEmail::class.java)
        (parsed.email as ValidEmail).let {
            assertAll(
                { assertThat(it.user).isEqualTo("hello") },
                { assertThat(it.domain).isEqualTo("hello.com") }
            )
        }
    }

    @Test
    fun testShouldFailWhenInvalidEmailInController() {
        val controller = ControllerLikeRegistrationController()
        val result = controller.registerUser(getTestJson("invalid-email"))
        assertThat(result).isExactlyInstanceOf(Response.ErrorResponse::class.java)
        (result as Response.ErrorResponse).let {
            assertThat(it.errors).isNotEmpty
        }
    }

    private fun getTestJson(email: String) = """
                {
                    "email": "$email",
                    "anonymous": false,
                    "name": "Myname"
                }
            """.trimIndent()
}