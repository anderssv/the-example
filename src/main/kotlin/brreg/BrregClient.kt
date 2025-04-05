package brreg

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
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
    }

    /**
     * Creates a new BrregClientImpl with a default HttpClient.
     */
    constructor() : this(HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    })

    override suspend fun getEntity(organizationNumber: String): BrregEntity? {
        return try {
            client.get("$BASE_URL/$organizationNumber").body()
        } catch (e: Exception) {
            // Handle exceptions, e.g., log them
            null
        }
    }
}