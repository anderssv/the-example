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
    organisasjonsform: OrganisasjonsformDto = OrganisasjonsformDto(
        kode = "AS",
        beskrivelse = "Aksjeselskap",
        links = null
    ),
    registreringsdatoEnhetsregisteret: String = "2021-01-01",
    registrertIMvaregisteret: Boolean = true,
    naeringskode1: NaeringskodeDto? = NaeringskodeDto(
        kode = "62.010",
        beskrivelse = "Programmeringstjenester",
        links = null
    ),
    antallAnsatte: Int? = 10,
    forretningsadresse: AdresseDto? = AdresseDto(
        adresse = listOf("Testveien 1"),
        postnummer = "0123",
        poststed = "OSLO",
        kommunenummer = "0301",
        kommune = "OSLO",
        landkode = "NO",
        land = "Norge"
    ),
    stiftelsesdato: String? = "2020-01-01",
    institusjonellSektorkode: InstitusjonellSektorkodeDto? = InstitusjonellSektorkodeDto(
        kode = "2100",
        beskrivelse = "Private aksjeselskaper mv.",
        links = null
    ),
    registrertIForetaksregisteret: Boolean = true,
    registrertIStiftelsesregisteret: Boolean = false,
    registrertIFrivillighetsregisteret: Boolean = false,
    konkurs: Boolean = false,
    underAvvikling: Boolean = false,
    underTvangsavviklingEllerTvangsopplosning: Boolean = false,
    maalform: String = "NB",
    harRegistrertAntallAnsatte: Boolean = false,
    links: List<LinkDto>? = listOf(
        LinkDto(
            href = "https://data.brreg.no/enhetsregisteret/api/enheter/123456789",
            rel = "self",
            type = "application/json"
        )
    )
): BrregEntity {
    return BrregEntity(
        organisasjonsnummer = organisasjonsnummer,
        navn = navn,
        organisasjonsform = organisasjonsform,
        registreringsdatoEnhetsregisteret = registreringsdatoEnhetsregisteret,
        registrertIMvaregisteret = registrertIMvaregisteret,
        naeringskode1 = naeringskode1,
        antallAnsatte = antallAnsatte,
        forretningsadresse = forretningsadresse,
        stiftelsesdato = stiftelsesdato,
        institusjonellSektorkode = institusjonellSektorkode,
        registrertIForetaksregisteret = registrertIForetaksregisteret,
        registrertIStiftelsesregisteret = registrertIStiftelsesregisteret,
        registrertIFrivillighetsregisteret = registrertIFrivillighetsregisteret,
        konkurs = konkurs,
        underAvvikling = underAvvikling,
        underTvangsavviklingEllerTvangsopplosning = underTvangsavviklingEllerTvangsopplosning,
        maalform = maalform,
        harRegistrertAntallAnsatte = harRegistrertAntallAnsatte,
        links = links
    )
}
