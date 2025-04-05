package brreg

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*

/**
 * Client for accessing the Brreg API.
 */
interface BrregClient {
    /**
     * Fetches entity information by organization number.
     *
     * @param organizationNumber The organization number to fetch information for.
     * @return The entity information, or null if not found.
     */
    suspend fun getEntity(organizationNumber: String): BrregEntity?
}

/**
 * Implementation of the BrregClient interface using KTor.
 */
class BrregClientImpl(private val client: HttpClient) : BrregClient {
    companion object {
        private const val BASE_URL = "https://data.brreg.no/enhetsregisteret/api/enheter"

        fun client(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
            install(ContentNegotiation) {
                jackson {
                    // Configure Jackson to ignore unknown properties
                    configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            this.expectSuccess = false // Allow non-2xx responses
        }
    }

    /**
     * Creates a new BrregClientImpl with a default HttpClient.
     */
    constructor() : this(client(CIO.create()))

    override suspend fun getEntity(organizationNumber: String): BrregEntity? {
        val response  = client.get("$BASE_URL/$organizationNumber")
        return when {
            response.status.isSuccess() -> response.body()
            response.status.value in 400..499 -> null
            else -> throw Exception("Failed to fetch entity: ${response.status}")
        }
    }
}
