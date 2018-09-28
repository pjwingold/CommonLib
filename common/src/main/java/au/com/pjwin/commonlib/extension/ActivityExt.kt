package au.com.pjwin.commonlib.extension

import android.app.Activity
import android.content.Intent
import au.com.pjwin.commonlib.ui.Arg
import java.io.Serializable

inline fun <reified T : Activity> Activity.start(activityClass: Class<T>, vararg params: Serializable) {
    val intent = Intent(this, activityClass)

    params.forEachIndexed { index, element ->
        intent.putExtra(Arg.values()[index].name, element)
    }

    startActivity(intent)
}