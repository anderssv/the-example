package brreg

/**
 * Data model for the Brreg API response.
 * This is a simplified model based on the expected fields for a business entity.
 */
data class BrregEntity(
    val organisasjonsnummer: String,
    val navn: String,
    val organisasjonsform: OrganisasjonsformDto,
    val registreringsdatoEnhetsregisteret: String,
    val registrertIMvaregisteret: Boolean,
    val naeringskode1: NaeringskodeDto? = null,
    val antallAnsatte: Int? = null,
    val forretningsadresse: AdresseDto? = null,
    val stiftelsesdato: String? = null,
    val institusjonellSektorkode: InstitusjonellSektorkodeDto? = null,
    val registrertIForetaksregisteret: Boolean,
    val registrertIStiftelsesregisteret: Boolean,
    val registrertIFrivillighetsregisteret: Boolean,
    val konkurs: Boolean,
    val underAvvikling: Boolean,
    val underTvangsavviklingEllerTvangsopplosning: Boolean,
    val maalform: String,
    val harRegistrertAntallAnsatte: Boolean,
    val links: List<LinkDto>? = null
) {
    companion object
}

data class OrganisasjonsformDto(
    val kode: String,
    val beskrivelse: String,
    val links: List<LinkDto>? = null
) {
    companion object
}

data class NaeringskodeDto(
    val kode: String? = null,
    val beskrivelse: String? = null,
    val links: List<LinkDto>? = null
) {
    companion object
}

data class AdresseDto(
    val adresse: List<String>? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val kommunenummer: String? = null,
    val kommune: String? = null,
    val landkode: String? = null,
    val land: String? = null
) {
    companion object
}

data class InstitusjonellSektorkodeDto(
    val kode: String? = null,
    val beskrivelse: String? = null,
    val links: List<LinkDto>? = null
) {
    companion object
}

data class LinkDto(
    val href: String? = null,
    val rel: String? = null,
    val type: String? = null
) {
    companion object
}
