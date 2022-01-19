package me.rocka.fcitx5test.native

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

class FcitxDispatcher(private val controller: FcitxController) : Runnable, CoroutineDispatcher() {

    interface FcitxController {
        fun nativeStartup()
        fun nativeLoopOnce()
        fun nativeScheduleEmpty()
        fun nativeExit()
    }

    // not concurrent
    object Watchdog {
        private var installed: Job? = null
        private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        fun install() {
            if (installed != null)
                throw IllegalStateException("Watchdog has been installed!")
            installed = GlobalScope.launch(dispatcher) {
                delay(SINGLE_JOB_TIME_LIMIT)
                throw RuntimeException("Fcitx main thread had been blocked for $SINGLE_JOB_TIME_LIMIT ms!")
            }
        }

        fun teardown() {
            (installed ?: throw IllegalStateException("Watchdog has not been installed!")).cancel()
            installed = null
        }

        inline fun <T> withWatchdog(block: () -> T): T {
            install()
            try {
                return block()
            } finally {
                teardown()
            }
        }
    }

    private val lock = ReentrantLock()

    private val queue = ConcurrentLinkedQueue<Runnable>()

    private val _isRunning = AtomicBoolean(false)

    val isRunning: Boolean
        get() = _isRunning.get()

    override fun run() {
        Log.i(javaClass.name, "Start running")
        if (_isRunning.compareAndSet(false, true)) {
            Log.d(javaClass.name, "Calling native startup")
            controller.nativeStartup()
        }
        while (isRunning) {
            // blocking...
            lock.withLock {
                measureTimeMillis {
                    controller.nativeLoopOnce()
                }.let {
                    Log.d(
                        javaClass.name,
                        "Finishing executing native loop once, took $it ms"
                    )
                }
            }
            Watchdog.withWatchdog {
                // do scheduled jobs
                measureTimeMillis {
                    while (queue.peek() != null) {
                        val block = queue.poll()!!
                        block.run()
                    }
                }.let { Log.d(javaClass.name, "Finishing running scheduled jobs, took $it ms") }
            }
        }
        Log.i(javaClass.name, "Calling native exit")
        controller.nativeExit()
    }

    fun stop(): List<Runnable> =
        if (_isRunning.compareAndSet(true, false)) {
            val rest = queue.toList()
            queue.clear()
            rest
        } else emptyList()

    private fun dispatch(block: Runnable) {
        queue.offer(block)
        // bypass nativeLoopOnce if no code is executing in native dispatcher
        if (lock.isLocked)
            controller.nativeScheduleEmpty()
    }

    suspend fun <T> dispatch(block: () -> T): T = suspendCoroutine {
        dispatch(Runnable {
            it.resume(block())
        })
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch(block)
    }

    companion object {
        const val SINGLE_JOB_TIME_LIMIT = 5000L
    }

}