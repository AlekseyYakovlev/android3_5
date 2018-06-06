package ru.geekbrains.android3_5.model.repo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import ru.geekbrains.android3_5.model.entity.Repository;
import ru.geekbrains.android3_5.model.entity.User;
import ru.geekbrains.android3_5.model.entity.realm.RealmRepository;
import ru.geekbrains.android3_5.model.entity.realm.RealmUser;

class RealmDataCache implements DataCache {
    @Override
    public Observable<User> getUserFromDB(String username) {
        return Observable.create(emitter -> {

            Realm realm = Realm.getDefaultInstance();
            RealmUser realmUser = realm.where(RealmUser.class).equalTo("login", username).findFirst();
            if (realmUser == null) {
                emitter.onError(new RuntimeException("No such user: " + username));
            } else {
                emitter.onNext(new User(realmUser.getLogin(), realmUser.getAvatarUrl(), realmUser.getReposUrl()));
                emitter.onComplete();
            }
//            realm.close();
        });
    }

    @Override
    public User saveUserToDB(User user) {
        Realm realm = Realm.getDefaultInstance();
        RealmUser realmUser = realm.where(RealmUser.class).equalTo("login", user.getLogin()).findFirst();
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
    }

    @Override
    public Observable<List<Repository>> getUserReposFromDB(User user) {
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
    }

    @Override
    public List<Repository> saveReposToDB(User user, List<Repository> repositories) {
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
    }
}
