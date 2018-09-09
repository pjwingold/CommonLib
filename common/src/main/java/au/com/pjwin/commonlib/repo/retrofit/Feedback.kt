package au.com.pjwin.commonlib.repo.retrofit

open class Feedback<Model> {

    /**
     * Invoked regardless of the response or lack of it.
     * @param success true if successful response is received.
     */
    open fun received(success: Boolean) {
    }

    /**
     * Invoked only if a successful response from server or session cache (short duration) is received.
     * @param model response data
     */
    open fun success(model: Model?) {
    }

    /**
     * Invoked if the response received from server is erroneous, or no response is received.
     * @param throwable [RestException] for server errors, [java.io.IOException] for network issues.
     */
    open fun error(throwable: Throwable) {
    }

    //todo handle stale
}