package au.com.pjwin.commonlib.util

import androidx.annotation.IdRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import au.com.pjwin.commonlib.ui.BaseFragment
import au.com.pjwin.commonlib.ui.NavGraphModel
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkConstructor
import org.junit.Assert.assertEquals

abstract class BaseFragmentTest<T : BaseFragment<*, *, *>>(private val fragmentClz: Class<T>) : BaseTest() {

    protected lateinit var fragment: T
    protected var mockedVM: DataViewModel<T>? = null
    protected var navController: NavController? = null
    protected lateinit var enclosingActivity: TestActivity

    protected open fun navGraph() = NavGraphModel()

    protected open fun buildFragment(): T = fragmentClz.newInstance()

    override fun setup() {
        super.setup()
        buildTestActivity()
        enclosingActivity = activityController!!.get() as TestActivity
        fragment = buildFragment()
        initMockVM()
        attachFragment(fragment)
        attachNavController()
    }

    override fun cleanUp() {
        super.cleanUp()
        mockedVM?.run {
            unmockkConstructor(ViewModelProvider::class)
        }
    }

    protected fun verifyCurrentDestination(@IdRes id: Int) {
        assertEquals(id, navController?.currentDestination?.id)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun initMockVM() {
        val vmClass = fragment.getViewModelClass()
        if (vmClass.simpleName != "VoidViewModel") {
            mockViewModelConstructor()
            val actualVM = vmClass.newInstance()
            mockedVM = spyk(actualVM) as DataViewModel<T>
            every { anyConstructed<ViewModelProvider>().hint(actualVM::class).get(vmClass) } returns mockedVM!!
        }
    }

    protected open fun mockViewModelConstructor() {
        mockkConstructor(ViewModelProvider::class)
    }

    private fun attachFragment(fragment: T) {
        enclosingActivity.attachFragment(fragment)
        enclosingActivity.supportFragmentManager.executePendingTransactions()
    }

    protected fun <T : ViewDataBinding> getBinding(): T? =
            DataBindingUtil.getBinding(fragment.rootView()!!)

    private fun attachNavController() {
        val graph = navGraph()
        if (graph.graphId != 0) {
            navController = mockNavController(graph)
        }
    }

    private fun mockNavController(navGraphDetails: NavGraphModel): NavController {
        val navController = TestNavHostController(enclosingActivity)
        Navigation.setViewNavController(fragment.requireView(), navController)

        val navGraph = navController.navInflater.inflate(navGraphDetails.graphId)

        if (navGraphDetails.startDestinationId != 0) {
            navGraph.setStartDestination(navGraphDetails.startDestinationId)
        }

        navController.setGraph(navGraph, navGraphDetails.startDestinationArgs)

        return navController
    }
}