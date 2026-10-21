package nl.klrnbk.common.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

open class ApiException(
    val status: HttpStatus,
    message: String,
    val detail: String,
    instance: String? = null,
) : Exception(message) {
    val instance: String = instance ?: getExceptionInstance()

    companion object {
        fun getExceptionInstance(): String {
            val sra = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes)
            val request = sra.request
            return request.requestURI
        }
    }
}
