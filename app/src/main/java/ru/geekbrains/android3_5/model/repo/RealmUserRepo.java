package ru.geekbrains.android3_5.model.repo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import ru.geekbrains.android3_5.model.api.ApiHolder;
import ru.geekbrains.android3_5.model.common.NetworkStatus;
import ru.geekbrains.android3_5.model.entity.Repository;
import ru.geekbrains.android3_5.model.entity.User;
import ru.geekbrains.android3_5.model.entity.realm.RealmRepository;
import ru.geekbrains.android3_5.model.entity.realm.RealmUser;

public class RealmUserRepo {
    public Observable<User> getUser(String username) {
        if (NetworkStatus.isOffline()) {
            return Observable.create(emitter -> {

                Realm realm = Realm.getDefaultInstance();
                RealmUser realmUser = realm.where(RealmUser.class).equalTo("login", username).findFirst();
                if (realmUser == null) {
                    emitter.onError(new RuntimeException("No such user: " + username));
                } else {
                    emitter.onNext(new User(realmUser.getLogin(), realmUser.getAvatarUrl(), realmUser.getReposUrl()));
                    emitter.onComplete();
                }
            });
        } else {
            return ApiHolder.getApi().getUser(username).subscribeOn(Schedulers.io()).map(user -> {
                Realm realm = Realm.getDefaultInstance();
                RealmUser realmUser = realm.where(RealmUser.class).equalTo("login", username).findFirst();
                if (realmUser == null) {
                    realm.executeTransaction(innerRealm -> {
                        RealmUser newRealmUser = realm.createObject(RealmUser.class, user.getLogin());
                        newRealmUser.setAvatarUrl(user.getAvatarUrl());
                    });
                } else {
                    realm.executeTransaction(innerRealm -> realmUser.setAvatarUrl(user.getAvatarUrl()));
                }
                realm.close();
                return user;
            });
        }
    }

    public Observable<List<Repository>> getUserRepos(User user) {
        if (NetworkStatus.isOffline()) {
            return Observable.create(emitter -> {
                Realm realm = Realm.getDefaultInstance();
                RealmUser realmUser = realm.where(RealmUser.class).equalTo("login", user.getLogin()).findFirst();
                if (realmUser == null) {
                    emitter.onError(new RuntimeException("No such user: " + user.getLogin()));
                } else {
                    List<Repository> repositories = new ArrayList<>();
                    for (RealmRepository realmRepository : realmUser.getRepos()) {
                        repositories.add(new Repository(realmRepository.getId(), realmRepository.getName()));
                    }
                    emitter.onNext(repositories);
                    emitter.onComplete();
                }
            });
        } else {
            return ApiHolder.getApi().getUserRepos(user.getReposUrl()).subscribeOn(Schedulers.io()).map(repositories -> {

                Realm realm = Realm.getDefaultInstance();
                RealmUser realmUser = realm.where(RealmUser.class).equalTo("login", user.getLogin()).findFirst();
                if (realmUser == null) {
                    realm.executeTransaction(innerRealm -> {
                        RealmUser newRealmUser = realm.createObject(RealmUser.class, user.getLogin());
                        newRealmUser.setAvatarUrl(user.getAvatarUrl());
                    });
                }

                final RealmUser finalRealmUser = realm.where(RealmUser.class).equalTo("login", user.getLogin()).findFirst();
                realm.executeTransaction(innerRealm -> {
                    finalRealmUser.getRepos().deleteAllFromRealm();
                    for (Repository repository : repositories) {
                        RealmRepository realmRepository = innerRealm.createObject(RealmRepository.class, repository.getId());
                        realmRepository.setName(repository.getName());
                        finalRealmUser.getRepos().add(realmRepository);
                    }
                });

                realm.close();
                return repositories;
            });
        }
    }
}
