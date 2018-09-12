package au.com.pjwin.commonlib.util

import android.content.Context
import android.content.SharedPreferences

open class Pref {
    companion object {
        private val PREFERENCES_NAME = String.format("%s.%s", Util.context().applicationInfo.packageName, Util.context().applicationInfo.name)

        val SHARED_PREF: SharedPreferences = Util.context().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        operator fun set(key: Key, value: Any?) = SHARED_PREF.set(key, value)
    }

    interface Key {
        var value: String
    }
}

inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}

operator fun SharedPreferences.set(key: Pref.Key, value: Any?) {
    when (value) {
        is String? -> edit { it.putString(key.value, value) }
        is Int -> edit { it.putInt(key.value, value) }
        is Boolean -> edit { it.putBoolean(key.value, value) }
        is Float -> edit { it.putFloat(key.value, value) }
        is Long -> edit { it.putLong(key.value, value) }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

inline operator fun <reified T : Any> SharedPreferences.get(key: Pref.Key, defaultValue: T? = null): T? {
    return when (T::class) {
        String::class -> getString(key.value, defaultValue as? String) as T?
        Int::class -> getInt(key.value, defaultValue as? Int ?: -1) as T
        Boolean::class -> getBoolean(key.value, defaultValue as? Boolean ?: false) as T
        Float::class -> getFloat(key.value, defaultValue as? Float ?: -1f) as T
        Long::class -> getLong(key.value, defaultValue as? Long ?: -1) as T
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}
