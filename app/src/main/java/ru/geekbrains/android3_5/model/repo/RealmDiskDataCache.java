package ru.geekbrains.android3_5.model.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import ru.geekbrains.android3_5.model.entity.Repository;
import ru.geekbrains.android3_5.model.entity.User;
import ru.geekbrains.android3_5.model.entity.realm.RealmRepository;
import ru.geekbrains.android3_5.model.entity.realm.RealmUser;
import ru.geekbrains.android3_5.model.repo.Disk.DiskCache;
import timber.log.Timber;

class RealmDiskDataCache implements DataCache {
    @Override
    public Observable<User> getUserFromDB(String username) {
        return Observable.create(emitter -> {
            Timber.d("*********************** getting user from DB");
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
        Timber.d("*********************** Saving user");
        Realm realm = Realm.getDefaultInstance();
        RealmUser realmUser = realm.where(RealmUser.class).equalTo("login", user.getLogin()).findFirst();
        if (realmUser == null) {
            realm.executeTransaction(innerRealm -> {
                RealmUser newRealmUser = realm.createObject(RealmUser.class, user.getLogin());
                String cachedUrl = user.getAvatarUrl();
                try {
                    cachedUrl = DiskCache.getInstance()
                            .getCachedUrl(user.getAvatarUrl())
                            .firstOrError()
                            .blockingGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Timber.d("**********************1 " + cachedUrl);
                newRealmUser.setAvatarUrl(cachedUrl);
            });
        } else {
            realm.executeTransaction(innerRealm -> {
                String cachedUrl = user.getAvatarUrl();
                try {
                    cachedUrl = DiskCache.getInstance()
                            .getCachedUrl(user.getAvatarUrl())
                            .firstOrError()
                            .blockingGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Timber.d("**********************2 " + cachedUrl);
                realmUser.setAvatarUrl(cachedUrl);
            });

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
            saveUserToDB(user);
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
