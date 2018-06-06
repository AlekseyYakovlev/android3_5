package ru.geekbrains.android3_5.model.repo;

import java.util.List;

import io.reactivex.Observable;
import ru.geekbrains.android3_5.model.entity.Repository;
import ru.geekbrains.android3_5.model.entity.User;

interface DataCache {
    Observable<User> getUserFromDB(String username);

    User saveUserToDB(User user);

    Observable<List<Repository>> getUserReposFromDB(User user);

    List<Repository> saveReposToDB(User user, List<Repository> repositories);
}
