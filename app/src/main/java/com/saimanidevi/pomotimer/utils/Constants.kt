package com.saimanidevi.pomotimer.utils

import com.saimanidevi.pomotimer.ButtonState
import com.saimanidevi.pomotimer.State

object Constants {
    // Notification Constants
    const val NOTIFICATION_CHANNEL_NAME = "pomo_notif_name"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Displays a notification when timer stops"
    const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    const val NOTIFICATION_ID = 1
    const val NOTIF_PENDING_INTENT_REQ_CODE = 200
    val VIBRATE_PATTERN = longArrayOf(1000, 1000, 1000, 1000, 1000)

    // WorkManager - Notification Work Constants
    const val WORKER_KEY_STATE = "pomo_state"
    const val WORKER_KEY_SESSION = "pomo_session"
    const val NOTIFICATION_WORK = "notif_work"

    const val pomodoro: String = "pomodoro"
    const val short_break: String = "short_break"
    const val long_break: String = "long_break"
    const val bt_start: String = "start"
    const val bt_stop: String = "stop"
    const val bt_pause: String = "pause"
    const val bt_resume: String = "resume"

    val pomoStateMap: Map<String, State> = mapOf(
        pomodoro to State.POMODORO,
        short_break to State.SHORT_BREAK,
        long_break to State.LONG_BREAK
    )
    val pomoStringMap: Map<State, String> = mapOf(
        State.POMODORO to pomodoro,
        State.SHORT_BREAK to short_break,
        State.LONG_BREAK to long_break
    )

    val buttonStateMap: Map<String, ButtonState> = mapOf(
        bt_start to ButtonState.START,
        bt_pause to ButtonState.PAUSE,
        bt_resume to ButtonState.RESUME,
        bt_stop to ButtonState.STOP
    )
    val buttonStringMap: Map<ButtonState, String> = mapOf(
        ButtonState.START to bt_start,
        ButtonState.PAUSE to bt_pause,
        ButtonState.RESUME to bt_resume,
        ButtonState.STOP to bt_stop
    )

    // PreferenceDataStore Constants
    const val SESSION_KEY = "key_session"
    const val STATE_KEY = "key_state"

    // Service Intent
    const val TIMER_EXTRA = "timer"
    const val TIMER_STARTED_EXTRA = "timer"
    const val TIMER_INTERVAL: Long = 1000

    // SharedPref Keys
    const val KEY_TIMER_STARTED = "timer_started"
    const val KEY_TIME_LEFT = "time_left"
    const val KEY_END_TIME = "end_time"
    const val KEY_SESSION = "session"
    const val KEY_STATE = "state"
    const val KEY_BUTTON_STATE = "bt_state"
}