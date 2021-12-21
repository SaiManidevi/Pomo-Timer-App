package com.saimanidevi.pomotimer.utils

import android.content.Context
import android.util.Log
import java.lang.ref.WeakReference

/**
 * Referred from [https://gist.github.com/RahulSDeshpande/b6a4519d8e037210739a13961d0723f4]
 * Modified accordingly
 */

object PrefUtils {
    /**
     * Called to save supplied value in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param value   Value to save
     */
    fun save(context: Context, key: String, value: Any) {
        val contextWeakReference = WeakReference(context)
        if (contextWeakReference.get() != null) {
            val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            when (value) {
                is Int -> editor.putInt(key, value)
                is String -> editor.putString(key, value.toString())
                is Boolean -> editor.putBoolean(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Double -> editor.putLong(key, java.lang.Double.doubleToRawLongBits(value))
            }
            editor.apply()
        }
    }

    /**
     * Called to retrieve required value from shared preferences, identified by given key.
     * Default value will be returned of no value found or error occurred.
     *
     * @param context      Context of caller activity
     * @param key          Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or any error occurs
     */
    fun get(context: Context, key: String, defaultValue: Any): Any? {
        val contextWeakReference = WeakReference(context)
        if (contextWeakReference.get() != null) {
            val sharedPrefs =
                context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            try {
                when (defaultValue) {
                    is String -> return sharedPrefs.getString(key, defaultValue.toString())
                    is Int -> return sharedPrefs.getInt(key, defaultValue)
                    is Boolean -> return sharedPrefs.getBoolean(key, defaultValue)
                    is Long -> return sharedPrefs.getLong(key, defaultValue)
                    is Float -> return sharedPrefs.getFloat(key, defaultValue)
                    is Double -> return java.lang.Double.longBitsToDouble(
                        sharedPrefs.getLong(
                            key,
                            java.lang.Double.doubleToLongBits(defaultValue)
                        )
                    )
                }
            } catch (e: Exception) {
                e.message?.let { Log.e("Exception: ", it) }
                return defaultValue
            }

        }
        return defaultValue
    }

    fun hasKey(context: Context, key: String): Boolean {
        val contextWeakReference = WeakReference(context)
        if (contextWeakReference.get() != null) {
            val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            return prefs.contains(key)
        }
        return false
    }
}