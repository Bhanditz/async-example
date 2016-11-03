package com.yalantis.asyncdemo.github;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GithubApi {

    @GET("/orgs/{org}/repos?per_page=1024")
    Call<List<Repository>> getOrgRepos(@Path("org") String org);
}
