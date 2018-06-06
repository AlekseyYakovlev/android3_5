package ru.geekbrains.android3_5.model.entity.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmUser extends RealmObject {
    @PrimaryKey
    String login;
    String avatarUrl;
    String reposUrl;

    RealmList<RealmRepository> repos = new RealmList<>();

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getReposUrl() {
        return reposUrl;
    }

    public void setReposUrl(String reposUrl) {
        this.reposUrl = reposUrl;
    }

    public RealmList<RealmRepository> getRepos() {
        return repos;
    }

    public void setRepos(RealmList<RealmRepository> repos) {
        this.repos = repos;
    }
}
