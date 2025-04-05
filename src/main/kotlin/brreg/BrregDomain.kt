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
    val naeringskode1: NaeringskodeDto?,
    val antallAnsatte: Int?,
    val forretningsadresse: AdresseDto?,
    val stiftelsesdato: String?,
    val institusjonellSektorkode: InstitusjonellSektorkodeDto?,
    val registrertIForetaksregisteret: Boolean,
    val registrertIStiftelsesregisteret: Boolean,
    val registrertIFrivillighetsregisteret: Boolean,
    val konkurs: Boolean,
    val underAvvikling: Boolean,
    val underTvangsavviklingEllerTvangsopplosning: Boolean,
    val maalform: String,
    val harRegistrertAntallAnsatte: Boolean,
    val links: List<LinkDto>?
) {
    companion object
}

data class OrganisasjonsformDto(
    val kode: String,
    val beskrivelse: String,
    val links: List<LinkDto>?
) {
    companion object
}

data class NaeringskodeDto(
    val kode: String?,
    val beskrivelse: String?,
    val links: List<LinkDto>?
) {
    companion object
}

data class AdresseDto(
    val adresse: List<String>?,
    val postnummer: String?,
    val poststed: String?,
    val kommunenummer: String?,
    val kommune: String?,
    val landkode: String?,
    val land: String?
) {
    companion object
}

data class InstitusjonellSektorkodeDto(
    val kode: String?,
    val beskrivelse: String?,
    val links: List<LinkDto>?
) {
    companion object
}

data class LinkDto(
    val href: String?,
    val rel: String?,
    val type: String?
) {
    companion object
}
