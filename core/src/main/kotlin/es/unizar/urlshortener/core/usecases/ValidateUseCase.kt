package es.unizar.urlshortener.core.usecases

enum class ValidationResponse {
    VALID,
    UNSAFE
}

/* Given a url returns if it is valid: safe. */
interface ValidateUseCase {
    fun validate(url: String): ValidationResponse
    fun isSafe(url: String): ValidationResponse
}

/* Implementation of [ValidateUseCase]. */
class ValidateUseCaseImpl() : ValidateUseCase {
    override fun validate(url: String): ValidationResponse {
        return isSafe(url)
    }

    override fun isSafe(url: String): ValidationResponse {
        if (url.length % 2 == 0) {
            return ValidationResponse.VALID
        } else {
            return ValidationResponse.UNSAFE
        }
    }
}
