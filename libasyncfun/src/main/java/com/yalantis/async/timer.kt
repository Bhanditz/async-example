package com.yalantis.async

import java.util.concurrent.FutureTask

fun timer(t: Long): FutureTask<Unit> = FutureTask { Thread.sleep(t) }
