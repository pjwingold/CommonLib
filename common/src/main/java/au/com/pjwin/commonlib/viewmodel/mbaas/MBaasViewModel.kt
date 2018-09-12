package au.com.pjwin.commonlib.viewmodel.mbaas

import au.com.pjwin.commonlib.viewmodel.DataViewModel

/**
 * Mobile backend as a service, ie Firebase, Kumulos etc
 */
open class MBaasViewModel<Data> : DataViewModel<Data>() {

    override fun onData(data: Data?) {
        if (canCallback()) {
            super.onData(data)
        }
    }

    override fun onError(throwable: Throwable?) {
        if (canCallback()) {
            super.onError(throwable)
        }
    }

    override fun onComplete(success: Boolean) {
        if (canCallback()) {
            super.onComplete(success)
        }
    }

    override fun hideLoading() {
        if (canCallback()) {
            super.hideLoading()
        }
    }

    override fun showLoading() {
        if (canCallback()) {
            super.showLoading()
        }
    }
}