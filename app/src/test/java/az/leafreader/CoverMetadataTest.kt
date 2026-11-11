package az.leafreader

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class CoverMetadataTest {
    @Test fun changingCoverTwicePreservesFirstOriginal() {
        val record=JSONObject().put("cover","covers/extracted.jpg")
        CoverMetadata.setCustom(record,"covers/custom_first.jpg")
        CoverMetadata.setCustom(record,"covers/custom_second.jpg")
        assertEquals("covers/extracted.jpg",record.getString("originalCover"))
        assertEquals("covers/custom_second.jpg",record.getString("cover"))
        assertTrue(record.getBoolean("customCover"))
        CoverMetadata.restoreOriginal(record)
        assertEquals("covers/extracted.jpg",record.getString("cover"))
        assertFalse(record.has("originalCover"))
        assertFalse(record.has("customCover"))
    }
    @Test fun restoreEmptyOriginalCover() {
        val record=JSONObject().put("cover","")
        CoverMetadata.setCustom(record,"covers/custom_only.jpg")
        CoverMetadata.restoreOriginal(record)
        assertEquals("",record.getString("cover"))
    }
    @Test(expected=IllegalArgumentException::class)
    fun rejectUnexpectedCoverPath() {
        CoverMetadata.setCustom(JSONObject(),"../unsafe.jpg")
    }
}
