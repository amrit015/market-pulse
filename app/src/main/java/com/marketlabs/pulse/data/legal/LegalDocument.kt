package com.marketlabs.pulse.data.legal

import com.squareup.moshi.JsonClass

/**
 * One legal document (Privacy Policy or Terms & Conditions) as bundled in
 * `assets/legal_privacy.json` / `assets/legal_terms.json`. These files are the canonical copy: the
 * public web pages serve the same JSON, so the structure carries paragraphs and bullet lists as
 * separate block types rather than text with embedded bullets and newlines that a web renderer
 * would have to parse.
 *
 * [version] is the legal version the document text belongs to; it has to equal
 * [LegalRepository.CURRENT_LEGAL_VERSION] (checked by `LegalDocumentJsonTest`). [lastUpdated] is an
 * ISO-8601 date (`yyyy-MM-dd`).
 */
@JsonClass(generateAdapter = true)
data class LegalDocument(
    val version: Int,
    val lastUpdated: String,
    val title: String,
    val sections: List<LegalSection>
)

@JsonClass(generateAdapter = true)
data class LegalSection(
    val heading: String,
    val blocks: List<LegalBlock>
)

/**
 * A `paragraph` block carries [text]; a `list` block carries [items]. One flat class rather than a
 * polymorphic hierarchy because there are only two block types and Moshi's codegen has no
 * built-in discriminator support.
 */
@JsonClass(generateAdapter = true)
data class LegalBlock(
    val type: String,
    val text: String? = null,
    val items: List<String>? = null
) {
    companion object {
        const val TYPE_PARAGRAPH = "paragraph"
        const val TYPE_LIST = "list"
    }
}

/**
 * Renders a section's blocks in the plain-string convention `FormattedBodyText` parses (`\n\n`
 * between blocks, `• ` prefix per list line), so the shared document-sections screen lays a legal
 * document out exactly as it laid out the string-resource version.
 */
fun LegalSection.toBodyText(): String = blocks.joinToString(separator = "\n\n") { block ->
    when (block.type) {
        LegalBlock.TYPE_LIST -> block.items.orEmpty().joinToString(separator = "\n") { "• $it" }
        else -> block.text.orEmpty()
    }
}
