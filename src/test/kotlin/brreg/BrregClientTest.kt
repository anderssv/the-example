package brreg

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BrregClientTest {

    @Test
    fun shouldFetchEntityByOrganizationNumber() {
        runBlocking {
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
    }
    
    @Test
    fun shouldReturnNullWhenEntityNotFound() {
        runBlocking {
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
    }
    
    @Test
    fun shouldReturnNullWhenExceptionOccurs() {
        runBlocking {
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
    }
}