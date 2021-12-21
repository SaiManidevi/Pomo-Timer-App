package com.saimanidevi.pomotimer

import android.app.Application
import androidx.lifecycle.*
import androidx.work.*
import com.saimanidevi.pomotimer.utils.Constants
import com.saimanidevi.pomotimer.utils.Constants.NOTIFICATION_WORK
import com.saimanidevi.pomotimer.utils.Constants.WORKER_KEY_SESSION
import com.saimanidevi.pomotimer.utils.Constants.WORKER_KEY_STATE
import com.saimanidevi.pomotimer.utils.Constants.bt_start
import com.saimanidevi.pomotimer.utils.Constants.buttonStateMap
import com.saimanidevi.pomotimer.utils.Constants.buttonStringMap
import com.saimanidevi.pomotimer.utils.Constants.pomoStateMap
import com.saimanidevi.pomotimer.utils.Constants.pomoStringMap
import com.saimanidevi.pomotimer.utils.Constants.pomodoro
import com.saimanidevi.pomotimer.utils.CountDownTimerExt
import com.saimanidevi.pomotimer.utils.PrefUtils
import com.saimanidevi.pomotimer.workers.NotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // CountDownTimer
    private var timerExt: CountDownTimerExt? = null

    // Event that observes changes in the Pomodoro States
    private val _pomoState: MutableLiveData<State> = MutableLiveData(State.POMODORO)
    val pomoState: LiveData<State>
        get() = _pomoState

    // Count of number of sessions
    private val _numberOfSessions: MutableLiveData<Int> = MutableLiveData(DEFAULT_SESSION)
    val numberOfSessions: LiveData<Int> get() = _numberOfSessions

    // Event that observes changes in the Button States
    private var _buttonState: MutableLiveData<ButtonState> = MutableLiveData(ButtonState.START)
    val buttonState: LiveData<ButtonState>
        get() = _buttonState

    // Event that observes changes in the Timer count and updates the textview
    private val _timerText: MutableLiveData<String> = _pomoState.map { currentState ->
        if (!timerStarted) {
            timerExt?.cancelTimer() // If a timer is already running, cancel it
            _buttonState.value = ButtonState.START  // Reset the button state
        }
        return@map convertMilliToTime(getCurrentPomoTime(currentState))
    } as MutableLiveData<String>
    val timerText: LiveData<String>
        get() = _timerText

    private val timeLeftInMillis: MutableLiveData<Long> = MutableLiveData()

    // Variable to track if timer started or not
    var timerStarted: Boolean = false

    // Variable to check if button click was just done to enable clicked state
    var justEnableButtonCheck: Boolean = false

    // Variable to check if state must change after timeout
    var changeStateAfterTimerOut: Boolean = false

    // Workmanager instance
    private val workManager = WorkManager.getInstance(application)

    /**
     * Function that is called when the (Start) button is clicked
     * The button can be in any state and the function handles
     * updating the UI based on the button state
     */
    fun startButtonClick() {
        val currentStateTimeInMillis =
            _pomoState.value?.let { getCurrentPomoTime(it) } ?: POMODORO_TIME
        when (buttonState.value) {
            ButtonState.START, ButtonState.STOP -> {
                initCountDownTimer(currentStateTimeInMillis)
                // Start alarm manager with given millis
                timerExt?.start()
                updateButtonState()
            }
            ButtonState.PAUSE -> {
                timerExt?.pause()
                _buttonState.value = ButtonState.RESUME
                // Cancel the Notification worker, since it will continue to display notification
                // within the initial time period
                cancelNotificationWorkRequest()
            }
            ButtonState.RESUME -> {
                timerExt?.start()
                _buttonState.value = ButtonState.PAUSE
                // Reset the Notification work request
                // to display notification within the remaining time
                setNotificationWorkRequest(
                    timeLeftInMillis.value ?: getCurrentPomoTime(_pomoState.value ?: State.POMODORO)
                )
            }
        }
    }

    /**
     * Helper function to get the correct time for each Pomodoro state
     */
    private fun getCurrentPomoTime(state: State) =
        when (state) {
            State.POMODORO -> POMODORO_TIME
            State.SHORT_BREAK -> SHORT_BREAK_TIME
            State.LONG_BREAK -> LONG_BREAK_TIME
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

    /**
     * Helper method to start the CountDownTimer
     * @param timeInMillis - Count down the time starting from the value in this param
     */
    private fun initCountDownTimer(timeInMillis: Long) {
        if (_buttonState.value != ButtonState.RESUME)
            setNotificationWorkRequest(timeInMillis)
        timerStarted = true
        timerExt = object :
            CountDownTimerExt(mMillisInFuture = timeInMillis, mInterval = TIMER_INTERVAL) {
            override fun onTimerTick(millisUntilFinished: Long) {
                timeLeftInMillis.value = millisUntilFinished
                _timerText.value = convertMilliToTime(millisUntilFinished)
            }

            override fun onTimerFinish() {
                timerStarted = false
                _buttonState.value = ButtonState.STOP
                updateSessionCount()
            }
        }
    }

    private fun setNotificationWorkRequest(timeInMillis: Long) {
        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(createInputDataForState())
            .setInitialDelay(timeInMillis, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            NOTIFICATION_WORK,
            ExistingWorkPolicy.REPLACE,
            notificationRequest
        )
    }

    private fun cancelNotificationWorkRequest() = workManager.cancelUniqueWork(NOTIFICATION_WORK)

    private fun createInputDataForState(): Data {
        val currentPomoStateString = pomoStringMap[_pomoState.value] ?: pomodoro
        val builder = Data.Builder()
        builder.putString(WORKER_KEY_STATE, currentPomoStateString)
        builder.putInt(WORKER_KEY_SESSION, _numberOfSessions.value ?: DEFAULT_SESSION)
        return builder.build()
    }

    private fun updateButtonState() {
        _buttonState.value =
            if (_buttonState.value == ButtonState.START) ButtonState.PAUSE else ButtonState.START
    }

    fun updatePomodoroState(state: State) {
        justEnableButtonCheck = changeStateAfterTimerOut
        _pomoState.value = state
    }

    private fun updateSessionCount() {
        changeStateAfterTimerOut = true
        // Get the current number of sessions
        val currentSessionCount: Int = _numberOfSessions.value ?: DEFAULT_SESSION
        // Get the current Pomodoro State
        val currentState: State = _pomoState.value ?: State.POMODORO
        // Handle sessions based on the current state
        when (currentState) {
            State.POMODORO -> {
                // decide if SHORT BREAK or LONG BREAK is to be given
                if (currentSessionCount < 4) updatePomodoroState(State.SHORT_BREAK)
                else updatePomodoroState(State.LONG_BREAK)
            }
            State.SHORT_BREAK, State.LONG_BREAK -> {
                updatePomodoroState(State.POMODORO)
                // Update the number of sessions
                _numberOfSessions.value =
                    if (currentSessionCount >= 4) DEFAULT_SESSION
                    else currentSessionCount.plus(1)
            }
        }
    }

    fun getCurrentStateButtonId(): Int {
        justEnableButtonCheck = true
        return when (_pomoState.value) {
            State.POMODORO -> R.id.bt_pomo
            State.SHORT_BREAK -> R.id.bt_short_break
            State.LONG_BREAK -> R.id.bt_long_break
            else -> R.id.bt_pomo
        }
    }

    fun buttonClickPerformed() {
        changeStateAfterTimerOut = false
    }

    fun resetButtonCheckState() {
        justEnableButtonCheck = false
    }

    companion object {
        const val POMODORO_TIME: Long = 25 * 60000
        const val SHORT_BREAK_TIME: Long = 5 * 60000
        const val LONG_BREAK_TIME: Long = 30 * 60000
        const val TIMER_INTERVAL: Long = 1000

        /*const val POMODORO_TIME: Long = 10000
        const val SHORT_BREAK_TIME: Long = 5000
        const val LONG_BREAK_TIME: Long = 10000
        const val TIMER_INTERVAL: Long = 1000*/
        const val DEFAULT_SESSION = 1
    }

    fun readData() {
        timerStarted =
            PrefUtils.get(getApplication(), key = Constants.KEY_TIMER_STARTED, false) as Boolean
        // Get the state of the button
        val savedButtonStateString: String =
            PrefUtils.get(getApplication(), key = Constants.KEY_BUTTON_STATE, bt_start) as String
        _buttonState.value = buttonStateMap[savedButtonStateString]
        if (timerStarted) {
            // If timer has started but the user paused the timer and closed the app,
            // then simply restore the savedTimeLeft - So, let's check the Button State
            // Since, the [_buttonState] livedata saves the next state of the button,
            // we check if the button's next state is RESUME i.e it is currently Paused
            if (_buttonState.value == ButtonState.RESUME) {
                val savedTimeLeftInMillis: Long =
                    PrefUtils.get(
                        getApplication(),
                        key = Constants.KEY_TIME_LEFT,
                        getCurrentPomoTime(_pomoState.value ?: State.POMODORO)
                    ) as Long
                timeLeftInMillis.value = savedTimeLeftInMillis
                _timerText.postValue(convertMilliToTime(savedTimeLeftInMillis))
                initCountDownTimer(savedTimeLeftInMillis)
            } else {
                val endTime: Long =
                    PrefUtils.get(getApplication(), key = Constants.KEY_END_TIME, 0L) as Long
                // Calculate the time left in milliseconds
                val timeLeftInMillis: Long = endTime - System.currentTimeMillis()
                // Check if the timer got completed when the app was in the background
                // If not, then start CountDownTimer [timerExt] with the remaining timeLeftInMillis
                // Else reset the [timerStarted] variable to false
                if (timeLeftInMillis > 0) {
                    // start timer with this new time
                    initCountDownTimer(timeLeftInMillis)
                    timerExt?.start()
                } else {
                    timerStarted = false
                }
            }
        }
        val savedSession: Int =
            PrefUtils.get(getApplication(), key = Constants.KEY_SESSION, DEFAULT_SESSION) as Int
        _numberOfSessions.value = savedSession
        val savedStateString: String =
            PrefUtils.get(getApplication(), key = Constants.KEY_STATE, pomodoro) as String
        _pomoState.value = pomoStateMap[savedStateString]
    }

    fun saveData() {
        timerExt?.cancelTimer()
        // Save the boolean variable that is true if timer has started else false
        PrefUtils.save(getApplication(), key = Constants.KEY_TIMER_STARTED, timerStarted)
        if (timerStarted) {
            // Save the time left in milliseconds long variable
            val timeLeftInMillis =
                timeLeftInMillis.value ?: getCurrentPomoTime(_pomoState.value ?: State.POMODORO)
            PrefUtils.save(getApplication(), key = Constants.KEY_TIME_LEFT, timeLeftInMillis)
            // Save the end time (for calculating the timer again when app is opened)
            val endTime = System.currentTimeMillis() + timeLeftInMillis
            PrefUtils.save(getApplication(), key = Constants.KEY_END_TIME, endTime)
        }
        // Save the current session
        val sessionToSave: Int = _numberOfSessions.value ?: DEFAULT_SESSION
        PrefUtils.save(getApplication(), key = Constants.KEY_SESSION, sessionToSave)
        // Save the current state
        val stateStringToSave: String = pomoStringMap[_pomoState.value] ?: pomodoro
        PrefUtils.save(getApplication(), key = Constants.KEY_STATE, stateStringToSave)
        // Save the current button state
        val buttonStateStringToSave: String = buttonStringMap[_buttonState.value] ?: bt_start
        PrefUtils.save(getApplication(), key = Constants.KEY_BUTTON_STATE, buttonStateStringToSave)
    }

}

enum class State {
    POMODORO,
    SHORT_BREAK,
    LONG_BREAK
}

enum class ButtonState {
    START,
    STOP,
    PAUSE,
    RESUME
}