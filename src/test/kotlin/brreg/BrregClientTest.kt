package brreg

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BrregClientTest {
    @Test
    fun shouldFetchEntityByOrganizationNumber() =
        runTest {
            // Arrange: Create a mock HttpClient that returns a predefined response
            val mockEngine =
                MockEngine { request ->
                    // Verify the request URL
                    assertThat(request.url.toString()).isEqualTo("https://data.brreg.no/enhetsregisteret/api/enheter/112233445")

                    // Return a mock response
                    val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                    val responseBody =
                        """
                        {
                            "organisasjonsnummer": "112233445",
                            "navn": "Test Entity",
                            "organisasjonsform": {
                                "kode": "AS",
                                "beskrivelse": "Aksjeselskap"
                            },
                            "registreringsdatoEnhetsregisteret": "2021-01-01",
                            "registrertIMvaregisteret": true,
                            "registrertIForetaksregisteret": true,
                            "registrertIStiftelsesregisteret": false,
                            "registrertIFrivillighetsregisteret": false,
                            "konkurs": false,
                            "underAvvikling": false,
                            "underTvangsavviklingEllerTvangsopplosning": false,
                            "maalform": "NB",
                            "harRegistrertAntallAnsatte": false
                        }
                        """.trimIndent()

                    respond(
                        content = responseBody,
                        status = HttpStatusCode.OK,
                        headers = responseHeaders,
                    )
                }

            val brregClient = BrregClientImpl(BrregClientImpl.client(mockEngine))

            // Act: Call the method being tested
            val entity = brregClient.getEntity("112233445")

            // Assert: Verify the result
            assertThat(entity).isNotNull
            with(entity!!) {
                assertThat(organisasjonsnummer).isEqualTo("112233445")
                assertThat(navn).isEqualTo("Test Entity")
                assertThat(organisasjonsform.kode).isEqualTo("AS")
                assertThat(organisasjonsform.beskrivelse).isEqualTo("Aksjeselskap")
                assertThat(registreringsdatoEnhetsregisteret).isEqualTo("2021-01-01")
                assertThat(registrertIMvaregisteret).isTrue()
            }
        }

    @Test
    fun shouldReturnNullWhenEntityNotFound() =
        runTest {
            // Arrange: Create a mock HttpClient that returns a 404 response
            val mockEngine =
                MockEngine { request ->
                    // Verify the request URL
                    assertThat(request.url.toString()).isEqualTo("https://data.brreg.no/enhetsregisteret/api/enheter/999999999")

                    // Return a 404 response
                    respond(
                        content = "",
                        status = HttpStatusCode.NotFound,
                    )
                }

            val brregClient = BrregClientImpl(BrregClientImpl.client(mockEngine))

            // Act: Call the method being tested
            val entity = brregClient.getEntity("999999999")

            // Assert: Verify the result
            assertThat(entity).isNull()
        }

    @Test
    fun shouldThrowExceptionWhenExceptionOccurs() =
        runTest {
            // Arrange: Create a mock HttpClient that throws an exception
            val mockEngine =
                MockEngine { _ ->
                    throw Exception("Simulated exception")
                }

            val brregClient = BrregClientImpl(BrregClientImpl.client(mockEngine))

            // Act & Assert: Verify that an exception is thrown
            assertThrows<Exception> {
                brregClient.getEntity("112233445")
            }
        }

    @Test
    @Tag("integration")
    fun shouldFetchEntityFromRealApi() =
        runTest {
            // Arrange: Create a real BrregClient with default HttpClient
            val brregClient = BrregClientImpl()

            // Act: Call the method being tested with a real organization number (DNB Bank ASA)
            val entity = brregClient.getEntity("984851006")

            // Assert: Verify the result
            assertThat(entity).isNotNull
            with(entity!!) {
                assertThat(organisasjonsnummer).isEqualTo("984851006")
                assertThat(navn).isEqualTo("DNB BANK ASA")

                assertThat(registrertIForetaksregisteret).isTrue()
                assertThat(organisasjonsform.kode).isEqualTo("ASA")

                assertThat(forretningsadresse).isNotNull
                assertThat(naeringskode1?.kode).isEqualTo("64.190")
            }
        }

    @Test
    fun shouldReturnCorrectEntityFromFake() =
        runTest {
            // Arrange: Create a fake BrregClient and add an entity
            val brregClientFake = BrregClientFake()
            val testEntity =
                BrregEntity.valid(
                    organisasjonsnummer = "123456789",
                    navn = "Test Fake Entity",
                    antallAnsatte = 42,
                )
            brregClientFake.addEntity(testEntity)

            // Act: Call the method being tested
            val foundEntity = brregClientFake.getEntity("123456789")
            val notFoundEntity = brregClientFake.getEntity("999999999")

            // Assert: Verify the results
            assertThat(foundEntity).isNotNull
            with(foundEntity!!) {
                assertThat(organisasjonsnummer).isEqualTo("123456789")
                assertThat(navn).isEqualTo("Test Fake Entity")
                assertThat(antallAnsatte).isEqualTo(42)
            }

            // Verify that non-existent entity returns null
            assertThat(notFoundEntity).isNull()
        }
}
