package com.d4rk.cleaner.invalid.util;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import androidx.annotation.NonNull;
import com.d4rk.cleaner.invalid.model.MediaItem;
import java9.util.stream.Stream;
public final class MediaStoreUtils {
    private MediaStoreUtils() {
        throw new RuntimeException("Use static methods in this class only.");
    }
    public static Stream < MediaItem > getAllImages(@NonNull ContentResolver cr) {
        final Cursor cur = Images.Media.query(
                cr, Images.Media.EXTERNAL_CONTENT_URI, null);
        if (cur == null) {
            throw new RuntimeException("Cannot get cursor for querying.");
        }
        return Stream.generate(() -> {
            if (cur.moveToNext()) {
                final MediaItem result = MediaItem.fromCursor(cur);
                if (cur.isLast() && !cur.isClosed()) {
                    cur.close();
                }
                return result;
            } else {
                return null;
            }
        }).limit(cur.getCount()).onClose(() -> {
            if (!cur.isClosed()) {
                cur.close();
            }
        });
    }
    public static int deleteImage(@NonNull ContentResolver cr, long id) {
        final Uri uri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id);
        return cr.delete(uri, null, null);
    }
}