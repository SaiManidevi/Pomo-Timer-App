package com.saimanidevi.pomotimer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.saimanidevi.pomotimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        binding.btStart.setOnClickListener {
            // Update the button text
            viewModel.startButtonClick()
        }
        binding.btPomo.maxLines = 2
        binding.btShortBreak.maxLines = 2
        binding.btLongBreak.maxLines = 2
        binding.togglePomoTime.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                // Check if button clicked by the user and not just checked programmatically for UI
                if (!viewModel.justEnableButtonCheck) {
                    // Check if a timer is already started and show Alert dialog
                    if (viewModel.timerStarted) showAlertDialog(checkedId)
                    else updatePomodoroState(checkedId)
                } else viewModel.resetButtonCheckState()
            }
        }
        observePomodoroState()
        observeButtonState()
    }

    private fun updatePomodoroState(checkedId: Int) {
        when (checkedId) {
            R.id.bt_pomo -> viewModel.updatePomodoroState(State.POMODORO)
            R.id.bt_short_break -> viewModel.updatePomodoroState(State.SHORT_BREAK)
            R.id.bt_long_break -> viewModel.updatePomodoroState(State.LONG_BREAK)
        }
    }

    /**
     * This is a helper function that displays an Alert Dialog
     * when the user tries to move to another Pomodoro State
     * without completing the current state
     */
    private fun showAlertDialog(checkedId: Int) {
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.dialog_title)
                setMessage(R.string.dialog_message)
                setPositiveButton(R.string.ok) { dialog, _ ->
                    // User clicked OK button - Change to selected state
                    viewModel.timerStarted = false
                    updatePomodoroState(checkedId)
                    dialog.dismiss()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    // User cancelled the dialog
                    // Ensure that the current state's button is checked
                    binding.togglePomoTime.check(viewModel.getCurrentStateButtonId())
                    dialog.cancel()
                }
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog.show()
    }

    private fun observeButtonState() {
        viewModel.buttonState.observe(this) { buttonState ->
            binding.btStart.text = when (buttonState) {
                ButtonState.START -> "START"
                ButtonState.PAUSE -> "PAUSE"
                ButtonState.RESUME -> "RESUME"
                ButtonState.STOP -> "STOP"
                else -> "START"
            }
        }
    }

    private fun observePomodoroState() {
        viewModel.pomoState.observe(this) { state ->
            updateUI(state)
            if (viewModel.changeStateAfterTimerOut) {
                // If state has changed automatically as a result of timer finishing
                // check the button of the current state
                // (since button won't be checked by default unless a user clicks)
                binding.togglePomoTime.check(viewModel.getCurrentStateButtonId())
                viewModel.buttonClickPerformed()
            }
        }
    }

    private fun updateUI(pomodoroState: State) {
        // Set up default colors
        var color = ContextCompat.getColor(this, R.color.color_pomo_time)
        var textColor = ContextCompat.getColor(this, R.color.color_pomo_time)
        var cardBgColor = ContextCompat.getColor(this, R.color.color_card_pomo)
        // Update colors based on the Pomodoro State
        when (pomodoroState) {
            State.POMODORO -> {
                color = ContextCompat.getColor(this, R.color.color_pomo_time)
                textColor = ContextCompat.getColor(this, R.color.color_pomo_time)
                cardBgColor = ContextCompat.getColor(this, R.color.color_card_pomo)
            }
            State.SHORT_BREAK -> {
                color = ContextCompat.getColor(this, R.color.color_short_break)
                textColor = ContextCompat.getColor(this, R.color.color_short_break)
                cardBgColor = ContextCompat.getColor(this, R.color.color_card_short_break)
            }
            State.LONG_BREAK -> {
                color = ContextCompat.getColor(this, R.color.color_long_break)
                textColor = ContextCompat.getColor(this, R.color.color_long_break)
                cardBgColor = ContextCompat.getColor(this, R.color.color_card_long_break)
            }
        }
        binding.pomoParent.setBackgroundColor(color)
        binding.btStart.setTextColor(textColor)
        binding.pomoCard.setCardBackgroundColor(cardBgColor)
    }

    override fun onStart() {
        super.onStart()
        viewModel.readData()
        binding.togglePomoTime.check(viewModel.getCurrentStateButtonId())
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveData()
    }
}