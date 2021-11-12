package com.d4rk.cleaner.invalid.loader;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.BitmapFactory;
import com.d4rk.cleaner.invalid.model.MediaItem;
import com.d4rk.cleaner.invalid.util.MediaStoreUtils;
import java.util.List;
import java9.util.stream.Collectors;
import java9.util.stream.Stream;
public class InvalidImagesLoader extends AsyncTaskLoader < List < MediaItem >> {
    public InvalidImagesLoader(Context context) {
        super(context);
    }
    @Override
    public List < MediaItem > loadInBackground() {
        Stream< MediaItem > stream = MediaStoreUtils.getAllImages(getContext().getContentResolver())
                .filter(item -> {
                    try {
                        final BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(item.path, opts);
                        return opts.outWidth <= 0 || opts.outHeight <= 0;
                    } catch (Exception e) {
                        return true;
                    }
                });
        List < MediaItem > result = stream.collect(Collectors.toList());
        stream.close();
        return result;
    }
    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}