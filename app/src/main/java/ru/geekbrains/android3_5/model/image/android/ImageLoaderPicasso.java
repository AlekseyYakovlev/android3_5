package ru.geekbrains.android3_5.model.image.android;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ru.geekbrains.android3_5.model.image.ImageLoader;
import timber.log.Timber;

/**
 * Created by stanislav on 3/12/2018.
 */

public class ImageLoaderPicasso implements ImageLoader<ImageView> {
    @Override
    public void loadInto(@Nullable String url, ImageView container) {
        Timber.d("Loading from: " + url);

        Picasso.get()
                .load(url)
                .into(container);


    }
}
