package au.com.pjwin.commonlib.extension

import au.com.pjwin.commonlib.ui.BaseActivity
import au.com.pjwin.commonlib.ui.BaseFragment

/**
 * Before getDataActivity<ParentDataActivity>().start()
 * Now dataActivity.start()
 */
inline val BaseFragment<*, *, *>.baseActivity: BaseActivity<*, *, *>
    get() = getBaseActivity()
