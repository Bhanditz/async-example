package com.yalantis.asyncdemo

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.yalantis.async.Wrapper
import com.yalantis.async.asyncFun
import com.yalantis.asyncdemo.github.GitHubRetrofit
import com.yalantis.asyncdemo.github.Repository
import java.util.concurrent.FutureTask

fun report(msg: String) {
    Log.d("REPORT", "$msg [on ${Thread.currentThread()}")
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val handler = Handler(mainLooper)
        val wrapper: Wrapper = { f: () -> Unit ->
            handler.post(f)
        }

        val gitHubApi = GitHubRetrofit().api
        val companies = listOf("yalantis", "google")

        report("before async block")
        asyncFun(wrapper) {
            report("before flatMap")
            val repos: List<Repository> = companies.flatMap { company ->
                try {
                    awaitFun {
                        report("loading repos for $company")
                        gitHubApi.getOrgRepos(company).execute().body()
                    }
                } catch (e: Exception) {
                    report("got $e")
                    throw e
                }
            }
            report("after flatMap")

            report("repos count: ${repos.size}")
        }
        report("after async block")

    }

}

