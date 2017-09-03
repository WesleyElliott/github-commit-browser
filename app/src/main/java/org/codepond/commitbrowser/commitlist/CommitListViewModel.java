/*
 * Copyright 2017 Nimrod Dayan CodePond.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codepond.commitbrowser.commitlist;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import org.codepond.commitbrowser.api.GithubApi;
import org.codepond.commitbrowser.common.recyclerview.Item;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class CommitListViewModel extends ViewModel {
    private GithubApi githubApi;
    private ObservableList<Item> commits = new ObservableArrayList<>();
    private int page = 1;

    public CommitListViewModel(GithubApi githubApi) {
        this.githubApi = githubApi;
    }

    public Observable<List<CommitItem>> loadCommits() {
        Timber.v("Request commit list");
        return githubApi.getCommits(page)
                .flatMap(Observable::from)
                .map(CommitItem::new)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(commitItems -> {
                    Timber.v("Received commit list. Size: %d", commitItems.size());
                    commits.addAll(commitItems);
                    page++;
                });
    }

    public ObservableList<Item> getCommits() {
        return commits;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private GithubApi githubApi;

        @Inject
        public Factory(GithubApi githubApi) {
            this.githubApi = githubApi;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new CommitListViewModel(githubApi);
        }
    }
}
