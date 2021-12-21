package com.saimanidevi.pomotimer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData
import com.saimanidevi.pomotimer.utils.Constants.TIMER_EXTRA
import com.saimanidevi.pomotimer.utils.Constants.TIMER_STARTED_EXTRA

class TimerReceiver : BroadcastReceiver() {
    private val mTimerText: MutableLiveData<String> = MutableLiveData<String>()
    val timerText: LiveData<String> get() = mTimerText

    // Variable to track if timer started or not
    private val mTimerStarted: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val timerStarted: LiveData<Boolean> get() = mTimerStarted

    override fun onReceive(context: Context, intent: Intent) {
        mTimerText.value = intent.getStringExtra(TIMER_EXTRA) ?: "00:00"
        mTimerStarted.value = intent.getBooleanExtra(TIMER_STARTED_EXTRA, false)
    }
}