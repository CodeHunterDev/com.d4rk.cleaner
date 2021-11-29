package com.d4rk.cleaner.invalid.util
import com.d4rk.cleaner.invalid.model.MediaItem.Companion.fromCursor
import android.content.ContentResolver
import android.provider.MediaStore.Images
import android.content.ContentUris
import com.d4rk.cleaner.invalid.model.MediaItem
import java9.util.stream.Stream
import java.lang.RuntimeException
class MediaStoreUtils private constructor() {
    companion object {
        @JvmStatic
        fun getAllImages(cr: ContentResolver): Stream<MediaItem?>? {
            val cur = Images.Media.query(cr, Images.Media.EXTERNAL_CONTENT_URI, null) ?: throw RuntimeException("Cannot get cursor for querying.")
            return Stream.generate {
                if (cur.moveToNext()) {
                    val result = fromCursor(cur)
                    if (cur.isLast && !cur.isClosed) {
                        cur.close()
                    }
                    return@generate result
                } else {
                    return@generate null
                }
            }.limit(cur.count.toLong()).onClose {
                if (!cur.isClosed) {
                    cur.close()
                }
            }
        }
        @JvmStatic
        fun deleteImage(cr: ContentResolver, id: Long): Int {
            val uri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id)
            return cr.delete(uri, null, null)
        }
    }
    init {
        throw RuntimeException("Use static methods in this class only.")
    }
}