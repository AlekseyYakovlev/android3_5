package ru.geekbrains.android3_5.presenter;

import ru.geekbrains.android3_5.view.RepoRowView;

public interface IRepoListPresenter {
    void bindRepoListRow(int pos, RepoRowView rowView);

    int getRepoCount();
}
