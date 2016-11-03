package com.yalantis.asyncdemo.github;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitHubRetrofit {
    private final Retrofit builder;

    public GitHubRetrofit() {
        Retrofit.Builder builder = new Retrofit.Builder();
        this.builder = builder.baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public GithubApi getApi() {
        return builder.create(GithubApi.class);
    }
}
