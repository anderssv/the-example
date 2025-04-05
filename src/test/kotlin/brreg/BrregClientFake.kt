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

    /**
     * Creates a default entity for the given organization number.
     *
     * @param organizationNumber The organization number to create an entity for.
     * @return The created entity.
     */
    fun createDefaultEntity(organizationNumber: String): BrregEntity {
        val entity = BrregEntity(
            organisasjonsnummer = organizationNumber,
            navn = "Test Entity $organizationNumber",
            organisasjonsform = OrganisasjonsformDto.valid(),
            registreringsdatoEnhetsregisteret = "2021-01-01",
            registrertIMvaregisteret = true,
            registrertIForetaksregisteret = true,
            registrertIStiftelsesregisteret = false,
            registrertIFrivillighetsregisteret = false,
            konkurs = false,
            underAvvikling = false,
            underTvangsavviklingEllerTvangsopplosning = false,
            maalform = "NB",
            harRegistrertAntallAnsatte = false
        )
        addEntity(entity)
        return entity
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
    organisasjonsform: OrganisasjonsformDto = OrganisasjonsformDto.valid(),
    registreringsdatoEnhetsregisteret: String = "2021-01-01",
    registrertIMvaregisteret: Boolean = true,
    naeringskode1: NaeringskodeDto? = NaeringskodeDto.valid(),
    antallAnsatte: Int? = 10,
    forretningsadresse: AdresseDto? = AdresseDto.valid(),
    stiftelsesdato: String? = "2020-01-01",
    institusjonellSektorkode: InstitusjonellSektorkodeDto? = InstitusjonellSektorkodeDto.valid(),
    registrertIForetaksregisteret: Boolean = true,
    registrertIStiftelsesregisteret: Boolean = false,
    registrertIFrivillighetsregisteret: Boolean = false,
    konkurs: Boolean = false,
    underAvvikling: Boolean = false,
    underTvangsavviklingEllerTvangsopplosning: Boolean = false,
    maalform: String = "NB",
    harRegistrertAntallAnsatte: Boolean = false,
    links: List<LinkDto>? = listOf(LinkDto.valid())
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

/**
 * Extension function to create a valid OrganisasjonsformDto for testing.
 */
fun OrganisasjonsformDto.Companion.valid(
    kode: String = "AS",
    beskrivelse: String = "Aksjeselskap",
    links: List<LinkDto>? = null
): OrganisasjonsformDto {
    return OrganisasjonsformDto(
        kode = kode,
        beskrivelse = beskrivelse,
        links = links
    )
}

/**
 * Extension function to create a valid NaeringskodeDto for testing.
 */
fun NaeringskodeDto.Companion.valid(
    kode: String? = "62.010",
    beskrivelse: String? = "Programmeringstjenester",
    links: List<LinkDto>? = null
): NaeringskodeDto {
    return NaeringskodeDto(
        kode = kode,
        beskrivelse = beskrivelse,
        links = links
    )
}

/**
 * Extension function to create a valid AdresseDto for testing.
 */
fun AdresseDto.Companion.valid(
    adresse: List<String>? = listOf("Testveien 1"),
    postnummer: String? = "0123",
    poststed: String? = "OSLO",
    kommunenummer: String? = "0301",
    kommune: String? = "OSLO",
    landkode: String? = "NO",
    land: String? = "Norge"
): AdresseDto {
    return AdresseDto(
        adresse = adresse,
        postnummer = postnummer,
        poststed = poststed,
        kommunenummer = kommunenummer,
        kommune = kommune,
        landkode = landkode,
        land = land
    )
}

/**
 * Extension function to create a valid InstitusjonellSektorkodeDto for testing.
 */
fun InstitusjonellSektorkodeDto.Companion.valid(
    kode: String? = "2100",
    beskrivelse: String? = "Private aksjeselskaper mv.",
    links: List<LinkDto>? = null
): InstitusjonellSektorkodeDto {
    return InstitusjonellSektorkodeDto(
        kode = kode,
        beskrivelse = beskrivelse,
        links = links
    )
}

/**
 * Extension function to create a valid LinkDto for testing.
 */
fun LinkDto.Companion.valid(
    href: String? = "https://data.brreg.no/enhetsregisteret/api/enheter/123456789",
    rel: String? = "self",
    type: String? = "application/json"
): LinkDto {
    return LinkDto(
        href = href,
        rel = rel,
        type = type
    )
}
