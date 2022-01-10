package es.unizar.urlshortener.infrastructure.delivery

import com.maxmind.geoip2.DatabaseReader
import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.*
import io.micrometer.core.instrument.MeterRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.mockito.BDDMockito.*
import org.mockito.kotlin.verify
import org.mockito.kotlin.willReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
@ContextConfiguration(classes = [
    UrlShortenerControllerImpl::class,
    RestResponseEntityExceptionHandler::class])
class UrlShortenerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var redirectUseCase: RedirectUseCase

    @MockBean
    private lateinit var logClickUseCase: LogClickUseCase

    @MockBean
    private lateinit var createShortUrlUseCase: CreateShortUrlUseCase

    @MockBean
    private lateinit var databaseReader: DatabaseReader

    @MockBean
    private lateinit var validateUseCase: ValidateUseCase

    @MockBean
    private lateinit var  reachableUrlUseCase: ReachableUrlUseCase

    @MockBean
    private lateinit var infolUseCase: RecoverInfoUseCase

    @MockBean
    private lateinit var meterRegistry: MeterRegistry

    @MockBean
    private lateinit var createQrUseCase: CreateQrUseCase

    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        given(redirectUseCase.redirectTo("key")).willReturn(Redirection("http://example.com/"))
        given(validateUseCase.isValidated("key")).willReturn(true)
        given(validateUseCase.isSafeAndReachable("key")).willReturn(true)
        
        mockMvc.perform(get("/tiny-{id}", "key"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(redirectedUrl("http://example.com/"))

        verify(logClickUseCase).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    @Test
    fun `redirectTo returns a redirect to an error page when the key exists but it isnt safe or reachable`() {

        given(validateUseCase.isValidated("key")).willReturn(true)
        given(validateUseCase.isSafeAndReachable("key")).willReturn(false)

        mockMvc.perform(get("/tiny-{id}", "key"))
                .andExpect(status().is3xxRedirection)
                .andExpect(redirectedUrl("http://localhost/errorp"))

        verify(logClickUseCase).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    @Test
    fun `redirectTo returns an error code 400 and a JSON message when the URL hasnt been validated yet`() {

        val expectedJson = "{\"error\" : \"URL de destino no validada todavia\"}";

        given(redirectUseCase.redirectTo("key")).willReturn(Redirection("http://example.com/"))
        given(validateUseCase.isValidated("key")).willReturn(false)

        mockMvc.perform(get("/tiny-{id}", "key"))
                .andDo(print())
                .andExpect(status().isBadRequest)
                .andExpect(content().json(expectedJson))

        verify(logClickUseCase).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }


    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        given(redirectUseCase.redirectTo("key"))
            .willAnswer { throw RedirectionNotFound("key") }

        mockMvc.perform(get("/tiny-{id}", "key"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.statusCode").value(404))

        verify(logClickUseCase, never()).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        given(createShortUrlUseCase.create(
            url = "http://example.com/",
            data = ShortUrlProperties(ip = "127.0.0.1")
        )).willReturn(ShortUrl("f684a3c4", Redirection("http://example.com/")))

        mockMvc.perform(post("/api/link")
            .param("url", "http://example.com/")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
            .andDo(print())
            .andExpect(redirectedUrl("http://localhost/tiny-f684a3c4"))
            .andExpect(jsonPath("$.url").value("http://localhost/tiny-f684a3c4"))
            .andExpect(status().isAccepted)
    }

    @Test
    fun `creates returns bad request if it can compute a hash`() {
        given(createShortUrlUseCase.create(
            url = "ftp://example.com/",
            data = ShortUrlProperties(ip = "127.0.0.1")
        )).willAnswer { throw InvalidUrlException("ftp://example.com/") }

        mockMvc.perform(post("/api/link")
                .param("url", "ftp://example.com/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `get qr returns not found if it has not been created before`() {
        given(createQrUseCase.get("key")).willReturn(null)

        mockMvc.perform(get("/qr/{hash}", "key"))
            .andDo(print())
            .andExpect(jsonPath("$.image").isEmpty)
            .andExpect(jsonPath("$.error").isNotEmpty)
            .andExpect(status().isNotFound)


    }

    @Test
    fun `get qr returns ok if it exists`() {
        given(createQrUseCase.get("key")).willReturn("image")

        mockMvc.perform(get("/qr/{hash}", "key"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.image").value("image"))
            .andExpect(jsonPath("$.error").isEmpty)

    }
}