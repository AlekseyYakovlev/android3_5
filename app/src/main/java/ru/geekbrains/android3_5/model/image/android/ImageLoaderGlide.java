package ru.geekbrains.android3_5.model.image.android;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.ByteArrayOutputStream;

import io.paperdb.Paper;
import ru.geekbrains.android3_5.model.common.NetworkStatus;
import ru.geekbrains.android3_5.model.common.Utils;
import ru.geekbrains.android3_5.model.image.ImageLoader;
import timber.log.Timber;

public class ImageLoaderGlide implements ImageLoader<ImageView> {
    @Override
    public void loadInto(@Nullable String url, ImageView container) {
        if (NetworkStatus.isOffline()) {
            String md5 = Utils.MD5(url);
            if (Paper.book("images").contains(md5)) {
                byte[] bytes = Paper.book("images").read(md5);
                Glide.with(container.getContext())
                        .load(bytes)
                        .into(container);
            }
        } else {
            GlideApp.with(container.getContext()).asBitmap().load(url).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    Timber.e(e, "Failed to load image");
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Paper.book("images").write(Utils.MD5(url), stream.toByteArray());
                    return false;
                }
            }).into(container);
        }
    }
}
