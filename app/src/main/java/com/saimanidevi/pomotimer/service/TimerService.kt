package com.saimanidevi.pomotimer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.saimanidevi.pomotimer.utils.Constants
import com.saimanidevi.pomotimer.utils.CountDownTimerExt
import java.util.*
import java.util.concurrent.TimeUnit

const val str_receiver: String = "com.saimanidevi.pomotimer.receiver"

class TimerService : Service() {
    private lateinit var intent: Intent

    // CountDownTimer
    private var timerExt: CountDownTimerExt? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        intent = Intent(str_receiver)
        timerExt = object :
            CountDownTimerExt(
                mMillisInFuture = 10000L,
                mInterval = Constants.TIMER_INTERVAL
            ) {
            override fun onTimerTick(millisUntilFinished: Long) {
                intent.putExtra(Constants.TIMER_STARTED_EXTRA, true)
                intent.putExtra(Constants.TIMER_EXTRA, convertMilliToTime(millisUntilFinished))
                sendBroadcast(intent)
            }

            override fun onTimerFinish() {
                intent.putExtra(Constants.TIMER_STARTED_EXTRA, false)
                sendBroadcast(intent)
                /*_buttonState.value = ButtonState.STOP
                updateSessionCount()*/
            }
        }
    }

    /**
     * Helper function to convert the given time in milliseconds
     * into Minutes and Seconds that is displayed on the UI
     */
    private fun convertMilliToTime(millisUntilFinished: Long) =
        java.lang.String.format(
            Locale.getDefault(),
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
            ),
            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    millisUntilFinished
                )
            )
        )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}