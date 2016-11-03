package com.yalantis.async

import java.util.concurrent.*

typealias Wrapper = (() -> Unit) -> Unit

fun asyncFun(
        wrapper: Wrapper? = null,
        coroutine c: FunController.() -> Continuation<Unit>
): Unit {
    val controller = FunController(wrapper)
    c(controller).resume(Unit)
}

@AllowSuspendExtensions
open class FunController(val wrapper: Wrapper? = null) {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(5)

    private fun wrapIfNeeded(block: () -> Unit) {
        wrapper?.invoke(block) ?: block()
    }

    suspend fun <V> awaitFun(f: () -> V, machine: Continuation<V>) {
        executorService.execute {
            try {
                val v = f()
                wrapIfNeeded { machine.resume(v) }
            } catch (e: Exception) {
                wrapIfNeeded { machine.resumeWithException(Exception(e.cause)) }
            }
        }
    }

    suspend fun <F, S> awaitFunPair(first: () -> F,
                                    second: () -> S,
                                    machine: Continuation<Pair<F, S>>) {
        val firstTask = FutureTask(first)
        val secondTask = FutureTask(second)

        executorService.execute(secondTask)

        executorService.execute {
            try {
                firstTask.run()
                val result = Pair(firstTask.get(), secondTask.get()) // blocking
                wrapIfNeeded { machine.resume(result) }
            } catch (e: ExecutionException) {
                wrapIfNeeded { machine.resumeWithException(Exception(e.cause)) }
            }
        }
    }

    suspend fun awaitFunAny(vararg fs: FutureTask<*>, machine: Continuation<FutureTask<*>>) {
        val ecs: ExecutorCompletionService<FutureTask<*>> = ExecutorCompletionService(executorService)

        fs.forEach { f ->
            ecs.submit { f.run(); f.get(); f }
        }

        executorService.execute(FutureTask(Callable {
            val f = ecs.take()

            fs.forEach { if (it != f) it.cancel(true) }

            wrapIfNeeded { machine.resume(f.get()) }
        }))
    }

}

