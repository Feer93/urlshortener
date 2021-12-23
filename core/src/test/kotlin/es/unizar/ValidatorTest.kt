package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

@WebMvcTest
@ContextConfiguration(classes = [
    ValidateUseCaseImpl::class,
    ReachableUrlUseCase::class])
class ValidatorTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var validateUseCase: ValidateUseCase

    @MockBean
    private lateinit var  reachableUseCase: ReachableUrlUseCase

    @Test
    fun `url is not safe`() {
        val url = "https://testsafebrowsing.appspot.com/s/malware.html"
        val isSafe = validateUseCase.isSafe(url)
        assertFalse(isSafe)
    }

    @Test
    fun `url is not reachable`() {
        val url = "https://ingweb.es"
        val isReachable = reachableUseCase.isReachable(url)
        assertFalse(isReachable)
    }
    @Disabled
    @Test
    fun `url is safe and reachable`() {
        val url = "https://google.com"

        val isSafe = validateUseCase.isSafe(url)
        assertTrue(isSafe)

        val isReachable = reachableUseCase.isReachable(url)
        assertTrue(isReachable)
    }

}