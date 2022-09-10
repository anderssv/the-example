import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver
import com.fasterxml.jackson.databind.jsontype.*
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

//@JsonTypeResolver(EmailResolver::class)
sealed class Email {
    data class ValidEmail(val value: String)
    data class InvalidEmail(val value: String) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun create(createValue: String): InvalidEmail {
                return InvalidEmail(createValue)
            }
        }
    }

}

class EmailResolver: TypeResolverBuilder<EmailResolver> {
    override fun getDefaultImpl(): Class<*> {
        return Email.InvalidEmail::class.java
    }

    override fun buildTypeSerializer(
        config: SerializationConfig?,
        baseType: JavaType?,
        subtypes: MutableCollection<NamedType>?
    ): TypeSerializer {
        TODO("Not yet implemented")
    }

    override fun buildTypeDeserializer(
        config: DeserializationConfig?,
        baseType: JavaType?,
        subtypes: MutableCollection<NamedType>?
    ): TypeDeserializer {
        TODO("Not yet implemented")
    }

    override fun init(idType: JsonTypeInfo.Id?, res: TypeIdResolver?): EmailResolver {
        TODO("Not yet implemented")
    }

    override fun inclusion(includeAs: JsonTypeInfo.As?): EmailResolver {
        TODO("Not yet implemented")
    }

    override fun typeProperty(propName: String?): EmailResolver {
        TODO("Not yet implemented")
    }

    override fun defaultImpl(defaultImpl: Class<*>?): EmailResolver {
        TODO("Not yet implemented")
    }

    override fun typeIdVisibility(isVisible: Boolean): EmailResolver {
        TODO("Not yet implemented")
    }

}

data class RegistrationForm(val email: Email.InvalidEmail, val anonymous: Boolean, val name: String)


class ParsingTest {


    @Test
    fun testShouldParseBasicJson() {
        val mapper = jacksonObjectMapper()
        val testJson = """
            {
                "email": "hello",
                "anonymous": false,
                "name": "Myname"
            }
        """.trimIndent()
        val parsed: RegistrationForm = mapper.readValue(testJson)

        assertThat(parsed.email.value).isEqualTo("hello")
    }

}