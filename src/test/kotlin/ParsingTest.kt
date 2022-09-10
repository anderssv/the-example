import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertAll
import kotlin.test.Test

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

class ParsingTest {

    @Test
    fun testShouldParseInvalidEmail() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(getTestJson("hello"))

        when (parsed.email) {
            is Email.InvalidEmail -> assertThat(parsed.email.value).isEqualTo("hello")
            else -> throw RuntimeException("Hello")
        }
    }

    @Test
    fun testShouldParseValidEmail() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(getTestJson("hello@hello.com"))

        when (parsed.email) {
            is Email.ValidEmail -> assertAll(
                { assertThat(parsed.email.user).isEqualTo("hello") },
                { assertThat(parsed.email.domain).isEqualTo("hello.com") }
            )

            else -> throw RuntimeException("Hello")
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