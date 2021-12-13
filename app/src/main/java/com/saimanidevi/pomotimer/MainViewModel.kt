package com.saimanidevi.pomotimer

import androidx.lifecycle.*
import com.saimanidevi.pomotimer.utils.CountDownTimerExt
import java.util.*
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {
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
        // If a timer is already running, cancel it
        timerExt?.cancelTimer()
        // Reset the button state
        _buttonState.value = ButtonState.START
        return@map convertMilliToTime(getCurrentPomoTime(currentState))
    } as MutableLiveData<String>
    val timerText: LiveData<String>
        get() = _timerText

    var timerStarted: Boolean = false
    var justEnableButtonCheck: Boolean = false
    var changeStateAfterTimerOut: Boolean = false

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
                startTimer(currentStateTimeInMillis)
                timerExt?.start()
                updateButtonState()
            }
            ButtonState.PAUSE -> {
                timerExt?.pause()
                _buttonState.value = ButtonState.RESUME
            }
            ButtonState.RESUME -> {
                timerExt?.start()
                _buttonState.value = ButtonState.PAUSE
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


    private fun startTimer(timeInMillis: Long) {
        timerStarted = true
        timerExt = object :
            CountDownTimerExt(mMillisInFuture = timeInMillis, mInterval = TIMER_INTERVAL) {
            override fun onTimerTick(millisUntilFinished: Long) {
                _timerText.value = convertMilliToTime(millisUntilFinished)
            }

            override fun onTimerFinish() {
                timerStarted = false
                _buttonState.value = ButtonState.STOP
                updateSessionCount()
            }
        }
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