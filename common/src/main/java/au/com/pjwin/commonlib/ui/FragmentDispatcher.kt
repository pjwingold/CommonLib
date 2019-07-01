package au.com.pjwin.commonlib.ui

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import au.com.pjwin.commonlib.R

internal class FragmentDispatcher(host: FragmentActivity) : LifecycleObserver {
    private var hostActivity: FragmentActivity? = host
    private var lifeCycle: Lifecycle? = host.lifecycle
    private val profilePendingList = mutableListOf<Fragment>()
    //prevent IllegalStateException: Can't change container ID of Fragment
    private val fragmentMap = HashMap<String, Int>()
    private val lock = Object()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        if (profilePendingList.isNotEmpty()) {
            val fragment = profilePendingList.last()
            fragmentMap[fragment.javaClass.name]?.let { container ->
                showFragment(container, fragment, false)
            }
        }
    }

    fun dispatcherFragment(@IdRes container: Int, fragment: Fragment, animate: Boolean) {
        synchronized(lock) {
            if (fragmentMap[fragment.javaClass.name] == null) {
                fragmentMap[fragment.javaClass.name] = container
            }
        }

        if (lifeCycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            showFragment(container, fragment, animate)

        } else {
            profilePendingList.clear()
            profilePendingList.add(fragment)
        }
    }

    private fun showFragment(fragment: Fragment) {
        showFragment(R.id.frame_layout, fragment, false)
    }

    private fun showFragment(@IdRes container: Int, fragment: Fragment, animate: Boolean) {
        hostActivity?.let {
            if (getExistingFragment<Fragment>(container) == null) {
                addFragment(container, fragment)

            } else {
                replaceFragment(container, fragment, animate)
            }
        }
    }

    private fun replaceFragment(@IdRes container: Int, fragment: Fragment, animate: Boolean) {
        hostActivity?.let {
            val fragmentTransaction = it.supportFragmentManager.beginTransaction()
            //Custom animations MUST be set before .replace() is called or they will fail.
            if (animate) {
                //todo setCustomAnimations(fragmentTransaction)
            }
            fragmentTransaction.replace(container, fragment, fragment.javaClass.name)

            val existing = getExistingFragment<Fragment>(container)
            if (existing != null) {//todo add checks to skip adding to backstack
                fragmentTransaction.addToBackStack(existing.javaClass.name)
            }

            fragmentTransaction.commit()
        }
    }

    private fun addFragment(@IdRes container: Int, fragment: Fragment) {
        hostActivity?.supportFragmentManager?.beginTransaction()
            ?.add(container, fragment, fragment.javaClass.name)
            ?.commit()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Fragment> getExistingFragment(@IdRes id: Int): T? {
        return hostActivity?.supportFragmentManager?.findFragmentById(id) as T?
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Fragment> getExistingFragment(tag: String): T? {
        return hostActivity?.supportFragmentManager?.findFragmentByTag(tag) as T?
    }

    /**
     * null out just in case
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        hostActivity = null
        lifeCycle = null
    }
}