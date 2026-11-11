package az.leafreader

import org.json.JSONObject

/** Keep original image metadata when custom covers are changed repeatedly. */
object CoverMetadata {
    fun setCustom(record:JSONObject,coverPath:String) {
        require(coverPath.startsWith("covers/custom_") && coverPath.endsWith(".jpg"))
        if(!record.has("originalCover"))record.put("originalCover",record.optString("cover"))
        record.put("cover",coverPath)
        record.put("customCover",true)
    }
    fun restoreOriginal(record:JSONObject) {
        if(record.has("originalCover")) {
            record.put("cover",record.optString("originalCover"))
            record.remove("originalCover")
        }
        record.remove("customCover")
    }
}
