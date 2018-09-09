package au.com.pjwin.commonlib.repo.retrofit

import android.support.annotation.VisibleForTesting
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@VisibleForTesting
internal class ImmediateExecutor : ExecutorService {

    override fun shutdown() {

    }

    override fun shutdownNow(): List<Runnable>? {
        return null
    }

    override fun isShutdown() = false

    override fun isTerminated() = false

    @Throws(InterruptedException::class)
    override fun awaitTermination(timeout: Long, unit: TimeUnit) = false

    override fun <T> submit(task: java.util.concurrent.Callable<T>): Future<T>? {
        return null
    }

    override fun <T> submit(task: Runnable, result: T): Future<T>? {
        return null
    }

    override fun submit(task: Runnable): Future<*>? {
        return null
    }

    @Throws(InterruptedException::class)
    override fun <T> invokeAll(tasks: Collection<java.util.concurrent.Callable<T>>): List<Future<T>>? {
        return null
    }

    @Throws(InterruptedException::class)
    override fun <T> invokeAll(tasks: Collection<java.util.concurrent.Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>>? {
        return null
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    override fun <T> invokeAny(tasks: Collection<java.util.concurrent.Callable<T>>): T? {
        return null
    }

    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    override fun <T> invokeAny(tasks: Collection<java.util.concurrent.Callable<T>>, timeout: Long, unit: TimeUnit): T? {
        return null
    }

    override fun execute(command: Runnable) {
        command.run()
    }
}