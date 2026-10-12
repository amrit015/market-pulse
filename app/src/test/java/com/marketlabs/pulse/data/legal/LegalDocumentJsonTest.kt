package com.marketlabs.pulse.data.legal

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

/**
 * Parses the real bundled legal assets straight off disk (plain JVM test, no AssetManager) with
 * the adapter `LegalDocumentProvider` uses at runtime, so a shape or version mismatch fails the
 * build instead of showing up as an empty Privacy screen or a re-acceptance loop on a device.
 */
class LegalDocumentJsonTest {

    private fun load(assetName: String): LegalDocument {
        val file = File("src/main/assets/$assetName")
        assertTrue("$assetName not found at ${file.absolutePath}", file.exists())
        val parsed = Moshi.Builder().build().adapter(LegalDocument::class.java).fromJson(file.readText())
        assertNotNull("Moshi failed to parse $assetName", parsed)
        return parsed!!
    }

    private val assets = listOf(LegalDocumentProvider.PRIVACY_ASSET, LegalDocumentProvider.TERMS_ASSET)

    @Test
    fun documentVersionMatchesCurrentLegalVersion() {
        assets.forEach { name ->
            assertEquals(
                "$name version drifted from LegalRepository.CURRENT_LEGAL_VERSION",
                LegalRepository.CURRENT_LEGAL_VERSION,
                load(name).version
            )
        }
    }

    @Test
    fun documentsHaveTitleDateAndWellFormedBlocks() {
        assets.forEach { name ->
            val doc = load(name)
            assertTrue("$name title blank", doc.title.isNotBlank())
            LocalDate.parse(doc.lastUpdated) // throws on a non-ISO date
            assertTrue("$name has no sections", doc.sections.isNotEmpty())
            doc.sections.forEach { section ->
                assertTrue("$name section heading blank", section.heading.isNotBlank())
                assertTrue("${section.heading} has no blocks", section.blocks.isNotEmpty())
                section.blocks.forEach { block ->
                    when (block.type) {
                        LegalBlock.TYPE_PARAGRAPH ->
                            assertFalse("${section.heading} blank paragraph", block.text.isNullOrBlank())
                        LegalBlock.TYPE_LIST ->
                            assertTrue("${section.heading} empty list", !block.items.isNullOrEmpty())
                        else -> throw AssertionError("${section.heading} unknown block type ${block.type}")
                    }
                }
            }
        }
    }

    @Test
    fun noEmbeddedBulletsOrNewlinesInBlockText() {
        assets.forEach { name ->
            load(name).sections.flatMap { it.blocks }.forEach { block ->
                val texts = listOfNotNull(block.text) + block.items.orEmpty()
                texts.forEach { text ->
                    assertFalse("$name block text contains a newline: $text", text.contains('\n'))
                    assertFalse("$name block text starts with a bullet: $text", text.trimStart().startsWith("•"))
                }
            }
        }
    }

    @Test
    fun toBodyTextRendersListsWithBulletPrefix() {
        val section = LegalSection(
            heading = "h",
            blocks = listOf(
                LegalBlock(LegalBlock.TYPE_PARAGRAPH, text = "intro"),
                LegalBlock(LegalBlock.TYPE_LIST, items = listOf("a", "b"))
            )
        )
        assertEquals("intro\n\n• a\n• b", section.toBodyText())
    }
}
