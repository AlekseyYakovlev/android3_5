package ru.geekbrains.android3_5.model.repo;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import ru.geekbrains.android3_5.model.api.ApiHolder;
import ru.geekbrains.android3_5.model.common.NetworkStatus;
import ru.geekbrains.android3_5.model.entity.Repository;
import ru.geekbrains.android3_5.model.entity.User;
import ru.geekbrains.android3_5.model.entity.activeandroid.AARepository;
import ru.geekbrains.android3_5.model.entity.activeandroid.AAUser;

public class AAUserRepo {
    public Observable<User> getUser(String username) {
        if (NetworkStatus.isOffline()) {
            return Observable.create(emitter -> {
                AAUser aaUser = new Select()
                        .from(AAUser.class)
                        .where("login = ?", username)
                        .executeSingle();

                if (aaUser == null) {
                    emitter.onError(new RuntimeException("No such user in cache: " + username));
                } else {
                    emitter.onNext(new User(aaUser.login, aaUser.avatarUrl, aaUser.reposUrl));
                    emitter.onComplete();
                }

            });
        } else {
            return ApiHolder.getApi().getUser(username).subscribeOn(Schedulers.io()).map(user -> {

                AAUser aaUser = new Select()
                        .from(AAUser.class)
                        .where("login = ?", username)
                        .executeSingle();

                if (aaUser == null) {
                    aaUser = new AAUser();
                    aaUser.login = user.getLogin();
                }

                aaUser.avatarUrl = user.getAvatarUrl();
                aaUser.save();
                return user;
            });
        }
    }

    public Observable<List<Repository>> getUserRepos(User user) {
        if (NetworkStatus.isOffline()) {
            return Observable.create(emitter -> {
                AAUser aaUser = new Select()
                        .from(AAUser.class)
                        .where("login = ?", user.getLogin())
                        .executeSingle();


                if (aaUser == null) {
                    emitter.onError(new RuntimeException("No such user in cache: " + user.getLogin()));
                } else {
                    List<Repository> repos = new ArrayList<>();
                    for (AARepository aaRepository : aaUser.repositories()) {
                        repos.add(new Repository(aaRepository.id, aaRepository.name));
                    }
                    emitter.onNext(repos);
                    emitter.onComplete();
                }
            });
        } else {
            return ApiHolder.getApi().getUserRepos(user.getReposUrl()).subscribeOn(Schedulers.io()).map(repositories -> {

                AAUser aaUser = new Select()
                        .from(AAUser.class)
                        .where("login = ?", user.getLogin())
                        .executeSingle();

                if (aaUser == null) {
                    aaUser = new AAUser();
                    aaUser.login = user.getLogin();
                    aaUser.avatarUrl = user.getAvatarUrl();
                    aaUser.save();
                }

                new Delete().from(AARepository.class).where("user = ?", aaUser.getId()).execute();

                ActiveAndroid.beginTransaction();
                try {
                    for (Repository repository : repositories) {
                        AARepository aaRepository = new AARepository();
                        aaRepository.id = repository.getId();
                        aaRepository.name = repository.getName();
                        aaRepository.user = aaUser;
                        aaRepository.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }


                return repositories;
            });
        }
    }
}
