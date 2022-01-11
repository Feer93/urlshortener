package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.*
import io.micrometer.core.instrument.MeterRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@WebMvcTest
@ContextConfiguration(classes = [
    StatsControllerImpl::class,
    RestResponseEntityExceptionHandler::class])
class StatsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var recoverInfoUseCase: RecoverInfoUseCase

    @MockBean
    private lateinit var meterRegistry: MeterRegistry

    //@Disabled
    @Test
    fun `general stats returned correctly`() {
        val description = "stats"
        val totalShortenedURL = 1L
        val totalClicks = 1L
        val top100clickedShortenedURL: MutableList<Pair<String, Long>> =
            mutableListOf(Pair("f684a3c4", 2), Pair("dfgh25as", 1))
        val top100hostsWithShortenedURL: MutableList<Pair<String, Long>> =
            mutableListOf(Pair("www.example.com", 2), Pair("www.unizar.es", 2))

        given(recoverInfoUseCase.countRedirection()).willReturn(totalClicks)
        given(recoverInfoUseCase.countURL()).willReturn(totalShortenedURL)
        given(recoverInfoUseCase.recoverTopKRedirection()).willReturn(top100clickedShortenedURL)
        given(recoverInfoUseCase.recoverTopKShortenedURL()).willReturn(top100hostsWithShortenedURL)


        mockMvc.perform(get("/{hash}.json", description))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description").value(description))
            .andExpect(jsonPath("$.totalShortenedURL").value(totalShortenedURL))
            .andExpect(jsonPath("$.totalClicks").value(totalClicks))
            .andExpect(jsonPath("$.top100clickedShortenedURL").isArray)
            .andExpect(jsonPath("$.top100hostsWithShortenedURL").isArray)

    }

    @Test
    fun `total shortened url stat returned correctly`() {
        val totalShortenedURL = 1L
        val id = "1"

        given(recoverInfoUseCase.countURL()).willReturn(totalShortenedURL)

        mockMvc.perform(get("/stat-{id}", id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statCount").value(totalShortenedURL))
            .andExpect(jsonPath("$.statID").value(id.toLong()))

    }

    @Test
    fun `total clicks stat returned correctly`() {
        val totalClicks = 1L
        val id = "2"

        given(recoverInfoUseCase.countRedirection()).willReturn(totalClicks)

        mockMvc.perform(get("/stat-{id}", id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statCount").value(totalClicks))
            .andExpect(jsonPath("$.statID").value(id.toLong()))

    }

    @Test
    fun `top 100 clicked shortened url stat returned correctly`() {
        val top100clickedShortenedURL: MutableList<Pair<String, Long>> =
            mutableListOf(Pair("f684a3c4", 2), Pair("dfgh25as", 1))
        val id = "3"

        given(recoverInfoUseCase.recoverTopKRedirection()).willReturn(top100clickedShortenedURL)

        mockMvc.perform(get("/stat-{id}", id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statList").isArray)
            .andExpect(jsonPath("$.statID").value(id.toLong()))

    }

    fun `top 100 hosts with shortened url stat returned correctly`() {
        val top100hostsWithShortenedURL: MutableList<Pair<String, Long>> =
            mutableListOf(Pair("www.example.com", 2), Pair("www.unizar.es", 2))
        val id = "4"

        given(recoverInfoUseCase.recoverTopKShortenedURL()).willReturn(top100hostsWithShortenedURL)

        mockMvc.perform(get("/stat-{id}", id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statList").isArray)
            .andExpect(jsonPath("$.statID").value(id.toLong()))

    }


}