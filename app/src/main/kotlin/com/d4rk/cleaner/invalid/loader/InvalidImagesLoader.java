package com.d4rk.cleaner.invalid.loader;
import android.content.Context;
import android.graphics.BitmapFactory;
import androidx.loader.content.AsyncTaskLoader;
import com.d4rk.cleaner.invalid.model.MediaItem;
import com.d4rk.cleaner.invalid.util.MediaStoreUtils;
import java.util.List;
import java.util.Objects;
import java9.util.stream.Collectors;
import java9.util.stream.Stream;
public class InvalidImagesLoader extends AsyncTaskLoader<List<MediaItem>> {
    public InvalidImagesLoader(Context context) {
        super(context);
    }
    @Override
    public List<MediaItem> loadInBackground() {
        Stream<MediaItem> stream = Objects.requireNonNull(MediaStoreUtils.getAllImages(getContext().getContentResolver()))
                .filter(item -> {
                    try {
                        final BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(item.path, opts);
                        if (opts.outWidth <= 0 || opts.outHeight <= 0) {
                            return true;
                        }
                        return opts.outMimeType == null;
                    } catch (Exception e) {
                        return true;
                    }
                });
        List<MediaItem> result = stream.collect(Collectors.toList());
        stream.close();
        return result;
    }
    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}