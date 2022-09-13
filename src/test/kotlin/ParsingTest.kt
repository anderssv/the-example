import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertAll
import kotlin.test.Test

class ParsingTest {
    private val repo = RegistrationRepository()
    private val controller = ControllerLikeRegistrationController(RegistrationService(repo))

    @Test
    fun testShouldParseInvalidEmail() {
        val mapper = jacksonObjectMapper()
        val parsed: RegistrationForm = mapper.readValue(getTestJson("hello"))

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.Invalid::class.java)
        (parsed as RegistrationForm.Invalid).let {
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

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.Invalid::class.java)
        (parsed as RegistrationForm.Invalid).let {
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

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.Invalid::class.java)
        (parsed as RegistrationForm.Invalid).let {
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

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.Invalid::class.java)
        (parsed as RegistrationForm.Invalid).let {
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

        assertThat(parsed).isExactlyInstanceOf(RegistrationForm.Valid.Registration::class.java)
        (parsed as RegistrationForm.Valid.Registration).let {
            assertAll({ assertThat(it.email.user).isEqualTo("hello") },
                { assertThat(it.email.domain).isEqualTo("hello.com") })
        }
    }

    @Test
    fun testShouldFailWhenInvalidEmailInController() {
        val result = controller.registerUser(getTestJson("invalid-email"))

        assertThat(result).isExactlyInstanceOf(ControllerResponse.ErrorResponse::class.java)
        (result as ControllerResponse.ErrorResponse).let {
            assertThat(it.errors).isNotEmpty
            assertThat(it.errors).contains(ValidationError("email", "Not a valid Email ;)", "invalid-email"))
        }
    }

    @Test
    fun testShouldStoreValidRegistrationInController() {
        val result = controller.registerUser(getTestJson("valid@mail.com"))

        assertThat(result).isExactlyInstanceOf(ControllerResponse.OkResponse::class.java)
        (result as ControllerResponse.OkResponse).let {
            assertThat(it.result).isEqualTo("Congrats Myname!")
        }
        assertThat(repo.getRegistration(Email.ValidEmail("valid", "mail.com"))).isNotNull
    }

    @Test
    fun testShouldStoreValidAnonymousRegistrationInController() {
        val result = controller.registerUser(getTestJson("valid@mail.com", addressJson = null, anonymous = true))

        assertThat(result).isExactlyInstanceOf(ControllerResponse.OkResponse::class.java)
        (result as ControllerResponse.OkResponse).let {
            assertThat(it.result).isEqualTo("Congrats!")
        }
        assertThat(repo.getRegistration(Email.ValidEmail("valid", "mail.com"))).isNotNull
    }

    private fun getTestJson(email: String, addressJson: String? = getAddressJson(), anonymous: Boolean = false) = """
                {
                    "email": "$email",
                    ${if (addressJson != null) "$addressJson," else ""}
                    "anonymous": $anonymous,
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