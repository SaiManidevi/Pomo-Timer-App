package com.saimanidevi.pomotimer.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.saimanidevi.pomotimer.MainViewModel
import com.saimanidevi.pomotimer.State
import com.saimanidevi.pomotimer.utils.Constants
import com.saimanidevi.pomotimer.utils.Constants.WORKER_KEY_SESSION
import com.saimanidevi.pomotimer.utils.Constants.WORKER_KEY_STATE
import com.saimanidevi.pomotimer.utils.Constants.pomoStateMap
import com.saimanidevi.pomotimer.utils.Constants.pomodoro
import com.saimanidevi.pomotimer.utils.NotificationUtils
import com.saimanidevi.pomotimer.utils.PrefUtils

class NotificationWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val pomoStateString: String = inputData.getString(WORKER_KEY_STATE) ?: pomodoro
        val previousPomoState: State = pomoStateMap[pomoStateString] ?: State.POMODORO
        val pomoSession: Int = inputData.getInt(WORKER_KEY_SESSION, MainViewModel.DEFAULT_SESSION)
        val nextPomodoroState: State = when (previousPomoState) {
            State.POMODORO -> {
                if (pomoSession >= 4) State.LONG_BREAK else State.SHORT_BREAK
            }
            State.SHORT_BREAK, State.LONG_BREAK -> State.POMODORO
        }
        val nextSession: Int = when (previousPomoState) {
            State.LONG_BREAK -> 1
            else -> pomoSession
        }
        return try {
            // Save the current session
            val sessionToSave: Int = nextSession
            PrefUtils.save(appContext, key = Constants.KEY_SESSION, sessionToSave)
            // Save the current state
            val stateStringToSave: String = Constants.pomoStringMap[nextPomodoroState] ?: pomodoro
            PrefUtils.save(appContext, key = Constants.KEY_STATE, stateStringToSave)
            NotificationUtils().displayNotification(
                context = appContext,
                nextPomodoroState,
                pomoSession
            )
            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }
}