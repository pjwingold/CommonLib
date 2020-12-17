package au.com.pjwin.commonlib.ui

import androidx.databinding.ViewDataBinding
import au.com.pjwin.commonlib.viewmodel.VoidViewModel

abstract class ViewActivity : BaseActivity<Void, VoidViewModel, ViewDataBinding>() {
    override fun layoutId() = 0
}