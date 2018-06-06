package ru.geekbrains.android3_5.model.repo;

import java.util.List;

import io.paperdb.Paper;
import io.reactivex.Observable;
import ru.geekbrains.android3_5.model.common.Utils;
import ru.geekbrains.android3_5.model.entity.Repository;
import ru.geekbrains.android3_5.model.entity.User;

class PaperDataCache implements DataCache {
    @Override
    public Observable<User> getUserFromDB(String username) {
        if (!Paper.book("users").contains(username)) {
            return Observable.error(new RuntimeException("No such user in cache: " + username));
        }

        return Observable.fromCallable(() -> Paper.book("users").read(username));
    }

    @Override
    public User saveUserToDB(User user) {
        Paper.book("users").write(user.getLogin(), user);
        return user;
    }

    @Override
    public Observable<List<Repository>> getUserReposFromDB(User user) {
        String md5 = Utils.MD5(user.getReposUrl());
        if (!Paper.book("repos").contains(md5)) {
            return Observable.error(new RuntimeException("No repos for such url: " + user.getReposUrl()));
        }

        return Observable.fromCallable(() -> Paper.book("repos").read(md5));
    }

    @Override
    public List<Repository> saveReposToDB(User user, List<Repository> repositories) {
        String md5 = Utils.MD5(user.getReposUrl());
        Paper.book("repos").write(md5, repositories);
        return repositories;
    }
}
