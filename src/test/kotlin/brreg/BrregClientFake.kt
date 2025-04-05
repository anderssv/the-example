package brreg

/**
 * Fake implementation of the BrregClient interface for testing.
 */
class BrregClientFake : BrregClient {
    private val entities = mutableMapOf<String, BrregEntity>()

    /**
     * Adds an entity to the fake client.
     *
     * @param entity The entity to add.
     */
    fun addEntity(entity: BrregEntity) {
        entities[entity.organisasjonsnummer] = entity
    }

    override suspend fun getEntity(organizationNumber: String): BrregEntity? {
        return entities[organizationNumber]
    }
}

/**
 * Extension function to create a valid BrregEntity for testing.
 */
fun BrregEntity.Companion.valid(
    organisasjonsnummer: String = "123456789",
    navn: String = "Test Entity",
    antallAnsatte: Int? = 10
): BrregEntity {
    return BrregEntity(
        organisasjonsnummer = organisasjonsnummer,
        navn = navn,
        organisasjonsform = OrganisasjonsformDto(
            kode = "AS",
            beskrivelse = "Aksjeselskap",
            links = null
        ),
        registreringsdatoEnhetsregisteret = "2021-01-01",
        registrertIMvaregisteret = true,
        naeringskode1 = NaeringskodeDto(
            kode = "62.010",
            beskrivelse = "Programmeringstjenester",
            links = null
        ),
        antallAnsatte = antallAnsatte,
        forretningsadresse = AdresseDto(
            adresse = listOf("Testveien 1"),
            postnummer = "0123",
            poststed = "OSLO",
            kommunenummer = "0301",
            kommune = "OSLO",
            landkode = "NO",
            land = "Norge"
        ),
        stiftelsesdato = "2020-01-01",
        institusjonellSektorkode = InstitusjonellSektorkodeDto(
            kode = "2100",
            beskrivelse = "Private aksjeselskaper mv.",
            links = null
        ),
        registrertIForetaksregisteret = true,
        registrertIStiftelsesregisteret = false,
        registrertIFrivillighetsregisteret = false,
        konkurs = false,
        underAvvikling = false,
        underTvangsavviklingEllerTvangsopplosning = false,
        maalform = "NB",
        harRegistrertAntallAnsatte = false,
        links = listOf(
            LinkDto(
                href = "https://data.brreg.no/enhetsregisteret/api/enheter/123456789",
                rel = "self",
                type = "application/json"
            )
        )
    )
}
