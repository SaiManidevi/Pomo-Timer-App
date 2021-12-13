package com.saimanidevi.pomotimer.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.saimanidevi.pomotimer.MainViewModel.Companion.DEFAULT_SESSION
import com.saimanidevi.pomotimer.R


@BindingAdapter("currentSession")
fun TextView.setCurrentSession(session: Int = DEFAULT_SESSION) {
    this.text = this.context.getString(R.string.current_session, session)
}
