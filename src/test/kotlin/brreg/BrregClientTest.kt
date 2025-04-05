package brreg

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Tag

class BrregClientTest {

    @Test
    fun shouldFetchEntityByOrganizationNumber() = runTest {
            // Arrange: Create a mock HttpClient that returns a predefined response
            val mockEngine = MockEngine { request ->
                // Verify the request URL
                assertThat(request.url.toString()).isEqualTo("https://data.brreg.no/enhetsregisteret/api/enheter/112233445")

                // Return a mock response
                val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                val responseBody = """
                    {
                        "organisasjonsnummer": "112233445",
                        "navn": "Test Entity",
                        "organisasjonsform": {
                            "kode": "AS",
                            "beskrivelse": "Aksjeselskap"
                        },
                        "registreringsdatoEnhetsregisteret": "2021-01-01",
                        "registrertIMvaregisteret": true
                    }
                """.trimIndent()

                respond(
                    content = responseBody,
                    status = HttpStatusCode.OK,
                    headers = responseHeaders
                )
            }

            val httpClient = HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    jackson()
                }
            }

            val brregClient = BrregClientImpl(httpClient)

            // Act: Call the method being tested
            val entity = brregClient.getEntity("112233445")

            // Assert: Verify the result
            assertThat(entity).isNotNull
            assertThat(entity?.organisasjonsnummer).isEqualTo("112233445")
            assertThat(entity?.navn).isEqualTo("Test Entity")
            assertThat(entity?.organisasjonsform?.kode).isEqualTo("AS")
            assertThat(entity?.organisasjonsform?.beskrivelse).isEqualTo("Aksjeselskap")
            assertThat(entity?.registreringsdatoEnhetsregisteret).isEqualTo("2021-01-01")
            assertThat(entity?.registrertIMvaregisteret).isTrue()
    }

    @Test
    fun shouldReturnNullWhenEntityNotFound() = runTest {
        // Arrange: Create a mock HttpClient that returns a 404 response
        val mockEngine = MockEngine { request ->
            // Verify the request URL
            assertThat(request.url.toString()).isEqualTo("https://data.brreg.no/enhetsregisteret/api/enheter/999999999")

            // Return a 404 response
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                jackson()
            }
        }

        val brregClient = BrregClientImpl(httpClient)

        // Act: Call the method being tested
        val entity = brregClient.getEntity("999999999")

        // Assert: Verify the result
        assertThat(entity).isNull()
    }

    @Test
    fun shouldReturnNullWhenExceptionOccurs() = runTest {
        // Arrange: Create a mock HttpClient that throws an exception
        val mockEngine = MockEngine { _ ->
            throw Exception("Simulated exception")
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                jackson()
            }
        }

        val brregClient = BrregClientImpl(httpClient)

        // Act: Call the method being tested
        val entity = brregClient.getEntity("112233445")

        // Assert: Verify the result
        assertThat(entity).isNull()
    }

    @Test
    @Tag("integration")
    fun shouldFetchEntityFromRealApi() = runTest {
        // Arrange: Create a real BrregClient with default HttpClient
        val brregClient = BrregClientImpl()

        // Act: Call the method being tested with a real organization number (DNB Bank ASA)
        val entity = brregClient.getEntity("984851006")

        // Assert: Verify the result
        assertThat(entity).isNotNull
        assertThat(entity?.organisasjonsnummer).isEqualTo("984851006")
        assertThat(entity?.navn).isEqualTo("DNB BANK ASA")

        // Verify organizational form
        assertThat(entity?.organisasjonsform).isNotNull
        assertThat(entity?.organisasjonsform?.kode).isEqualTo("ASA")
        assertThat(entity?.organisasjonsform?.beskrivelse).isEqualTo("Allmennaksjeselskap")

        // Verify address
        assertThat(entity?.forretningsadresse).isNotNull
        assertThat(entity?.forretningsadresse?.kommune).isEqualTo("OSLO")
        assertThat(entity?.forretningsadresse?.postnummer).isEqualTo("0191")

        // Verify that the entity is registered in MVA register
        assertThat(entity?.registrertIMvaregisteret).isTrue()

        // Verify business code
        assertThat(entity?.naeringskode1).isNotNull
        assertThat(entity?.naeringskode1?.kode).isEqualTo("64.190")
        assertThat(entity?.naeringskode1?.beskrivelse).contains("Bankvirksomhet")
    }
}
