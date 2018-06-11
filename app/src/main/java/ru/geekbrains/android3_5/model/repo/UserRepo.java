package ru.geekbrains.android3_5.model.repo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import ru.geekbrains.android3_5.model.api.ApiHolder;
import ru.geekbrains.android3_5.model.common.NetworkStatus;
import ru.geekbrains.android3_5.model.entity.Repository;
import ru.geekbrains.android3_5.model.entity.User;

public class UserRepo {

    private DataCache cacheDB;

    public UserRepo() {
        this.cacheDB = new RealmDataCache();
    }

    public UserRepo(CacheDbType cacheDbTypeType) {
        switch (cacheDbTypeType) {
            case AA:
                cacheDB = new AADataCache();
                break;
            case PAPER:
                cacheDB = new PaperDataCache();
                break;
            case REALM_DISK:
                cacheDB = new RealmDiskDataCache();
                break;
            default:
                cacheDB = new RealmDataCache();
                break;
        }
    }

    public Observable<User> getUser(String username) {
        if (NetworkStatus.isOffline()) {
            return cacheDB.getUserFromDB(username);
        } else {
            return ApiHolder.getApi()
                    .getUser(username)
                    .map(cacheDB::saveUserToDB)
                    .subscribeOn(Schedulers.io());
        }
    }

    public Observable<List<Repository>> getUserRepos(User user) {
        if (NetworkStatus.isOffline()) {
            return cacheDB.getUserReposFromDB(user);
        } else {
            return ApiHolder.getApi()
                    .getUserRepos(user.getReposUrl())
                    .subscribeOn(Schedulers.io())
                    .map(repositories -> cacheDB.saveReposToDB(user, repositories));
        }
    }

    public enum CacheDbType {AA, PAPER, REALM, REALM_DISK}
}
