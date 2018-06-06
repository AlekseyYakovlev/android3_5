package ru.geekbrains.android3_5.model.repo;

import java.util.List;

import io.paperdb.Paper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import ru.geekbrains.android3_5.model.api.ApiHolder;
import ru.geekbrains.android3_5.model.common.NetworkStatus;
import ru.geekbrains.android3_5.model.common.Utils;
import ru.geekbrains.android3_5.model.entity.Repository;
import ru.geekbrains.android3_5.model.entity.User;

public class PaperUserRepo {
    public Observable<User> getUser(String username) {
        if (NetworkStatus.isOffline()) {
            if (!Paper.book("users").contains(username)) {
                return Observable.error(new RuntimeException("No such user in cache: " + username));
            }

            return Observable.fromCallable(() -> Paper.book("users").read(username));
        } else {
            return ApiHolder.getApi().getUser(username).subscribeOn(Schedulers.io()).map(user -> {
                Paper.book("users").write(username, user);
                return user;
            });
        }
    }

    public Observable<List<Repository>> getUserRepos(User user) {
        String md5 = Utils.MD5(user.getReposUrl());
        if (NetworkStatus.isOffline()) {
            if (!Paper.book("repos").contains(md5)) {
                return Observable.error(new RuntimeException("No repos for such url: " + user.getReposUrl()));
            }

            return Observable.fromCallable(() -> Paper.book("repos").read(md5));
        } else {
            return ApiHolder.getApi().getUserRepos(user.getReposUrl()).subscribeOn(Schedulers.io()).map(repositories -> {
                Paper.book("repos").write(md5, repositories);
                return repositories;
            });
        }

    }
}
