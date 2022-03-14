package au.com.pjwin.commonlib.ui

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes

data class NavGraphModel(@NavigationRes val graphId: Int = 0,
                    @IdRes val startDestinationId: Int = 0,
                    val startDestinationArgs: Bundle? = null)