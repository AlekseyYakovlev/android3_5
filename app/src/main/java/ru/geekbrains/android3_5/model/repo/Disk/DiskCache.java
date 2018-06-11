package ru.geekbrains.android3_5.model.repo.Disk;

import android.graphics.Bitmap;
import android.os.Environment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DiskCache {

    private static DiskCache ourInstance;

    private DiskCache() {
    }

    public static DiskCache getInstance() {
        if (ourInstance == null)
            ourInstance = new DiskCache();
        return ourInstance;
    }

    public Observable<String> getCachedUrl(String avatarUrl) throws IOException {
        return Observable.just(Picasso.get().load(avatarUrl).get())
                .subscribeOn(Schedulers.io())
                .map(bitmap -> {
                    File file =
                            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                    "savedBitmap.png");
                    FileOutputStream fos = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    Timber.d("Success!");

                    return "file://" + file;
                });
    }
}
