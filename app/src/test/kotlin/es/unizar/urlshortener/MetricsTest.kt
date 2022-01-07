package es.unizar.urlshortener

import com.fasterxml.jackson.databind.JsonNode
import es.unizar.urlshortener.infrastructure.delivery.ErrorDataOut
import es.unizar.urlshortener.infrastructure.delivery.ShortUrlDataOut
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import com.fasterxml.jackson.databind.ObjectMapper
import es.unizar.urlshortener.infrastructure.delivery.QrDataOut
import org.junit.jupiter.api.Disabled


/**
 *
 * Documentación métricas
 *
 * Hay diferentes tipos de métricas según los usos, las cuales siguen la documentación descrita en
 * https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics.endpoint
 * teniendo en cuenta que el endpoint es en nuestra aplicación /metrics y no /actuator/metrics
 *
 * Las métricas presentes son las siguientes:
 *  - Todas las métricas por defecto del actuator, las cuales están en la documentación del actuator también
 *
 *  - Métricas que recogen tiempos de ejecución de los controladores en /method/timed , donde se puede
 *    elegir el metodo del que recuperar informacion, siendo recogida la informacion mediante la etiqueta
 *    @Timed por lo que para mas informacion mirar en la documentacion se Spring.
 *
 *  - Una métrica que sirve para registrar acciones de los usuarios accesible mediante /metrics/user.action y que tiene
 *    un tag llamado "type" para dividir esta métrica según el tipo de acción que puede ser: hacer click en una URL
 *    ("clickedURL"), crear una URL recortada ("createShortenedURL") o solicitar el qr de una URL ("qrUsed"). Un
 *    ejemplo de uso sería acceder a /metrics/user.action?tag=type:qrUsed , dónde se mostrará la información de la
 *    métrica "qrUsed". Esta metrica se basa en contadores.
 *
 *  - Una métrica que sirve para registrar los clicks de los usuarios URL especificas mediante /metrics/user.click.hash
 *    y que tiene un tag llamado "hash" para indicar el hash de la URL recortada del que se quiere recuperar información. Un
 *    Por ejemplo de uso sería acceder a /metrics/user.click.hash?tag=hash:<hash> , dónde se mostrará la información de
 *    la métrica para el hash introducido. Esta metrica se basa en contadores.
 *
 *  - Una métrica para registrar la longitud de la última URL a recortar en /metrics/shortener.last.url.length .
 *    A diferencia de otras métricas no hay ningún tag y está basada en un valor.
 *
 *  - Una métrica que sirve para registrar si una URL es valida o no mediante /metrics/validate.url y que tiene
 *    un tag llamado "type" para dividir esta métrica según si es valida la URL ("validURL") o no ("invalidURL").
 *    Un ejemplo de uso sería acceder a /metrics/validate.url?tag=type:invalidURL , dónde se mostrará la
 *    información de la métrica "invalidURL". Esta metrica se basa en contadores.
 *
 *  - Una métrica que sirve para registrar si una URL es alcanzable o no mediante /metrics/reach.url y que tiene
 *    un tag llamado "type" para dividir esta métrica según si es alcanzable la URL ("reachableURL") o no
 *    ("unreachableURL"). Un ejemplo de uso sería acceder a /metrics/reach.url?tag=type:unreachableURL , dónde se
 *    mostrará la información de la métrica "unreachableURL". Esta metrica se basa en contadores.
 *
 *
 * Documentación health
 *
 * Hay diversos componentes de los que dependen, entre ellos estan los componentes por defecto del endpoint y
 * tambien esta otro componente extra para ver si el servicio del que depende la comprobacion de la validez
 * de las URL a recortar esta caido o no ya que esto influye directamente en la aplicacion. Para mas informacion
 * consultar la documentacion de Spring: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
 * ,teniendo en cuenta que el endpoint es en nuestra aplicación /health y no /actuator/health
 *
 *
 */

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MetricsTest {


    @LocalServerPort
    var port = 0


    @Autowired
    lateinit var restTemplate: TestRestTemplate


    @Test
    fun `check it is up`() {

        val response = restTemplate.getForEntity("http://localhost:$port/health", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `check metrics work`() {

        //Make several requests to update metrics
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("url", "http://www.unizar.es")
        map.add("createQr", "true")

        //Shorten a URL
        val request: HttpEntity<MultiValueMap<String, String>> = HttpEntity(map, headers)
        val response = restTemplate.postForEntity("http://localhost:$port/api/link",
            request, ShortUrlDataOut::class.java)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("http://localhost:$port/tiny-4392f73f", response.body?.url.toString())
        assertEquals("http://localhost:8080/qr/4392f73f", response.body?.qr.toString())
        Thread.sleep(1000)

        //Access a shortened URL
        val response2 = restTemplate.getForEntity(response.body?.url.toString(), ErrorDataOut::class.java)
        assertEquals(HttpStatus.TEMPORARY_REDIRECT, response2.statusCode)

        /*
        //Access a qr created
        var response3 = restTemplate.getForEntity(response.body?.qr.toString(), QrDataOut::class.java)
        assertEquals(HttpStatus.OK, response3.statusCode)
        */


        //Comprobar que funciona el endpoint de métricas
        var response4 = restTemplate.getForEntity("http://localhost:$port/metrics/", String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)
        //assertEquals("", response4.body)

        //Check it has registered a shortened URL
        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/user.action?tag=type:createShortenedURL",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        val mapper = ObjectMapper()
        var actualObj: JsonNode = mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"COUNT\",\"value\":1.0}]", actualObj["measurements"].toString())

        //Check it has registered two total accesses to a URL (one for the qr and the other for the redirection)
        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/user.action?tag=type:clickedURL",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        actualObj= mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"COUNT\",\"value\":1.0}]", actualObj["measurements"].toString())

        /*
        //Check that the URL has been registered as valid
        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/validate.url?tag=type:validURL",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        actualObj= mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"COUNT\",\"value\":2.0}]", actualObj["measurements"].toString())

        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/validate.url?tag=type:invalidURL",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        actualObj= mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"COUNT\",\"value\":0.0}]", actualObj["measurements"].toString())


        //Check that the URL has been registered as reachable
        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/reach.url?tag=type:reachableURL",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        actualObj= mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"COUNT\",\"value\":1.0}]", actualObj["measurements"].toString())

        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/reach.url?tag=type:unreachableURL",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        actualObj= mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"COUNT\",\"value\":0.0}]", actualObj["measurements"].toString())
        */


        //Check that the length of the last URl sent has been updated
        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/shortener.last.url.length",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        actualObj= mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"VALUE\",\"value\":20.0}]", actualObj["measurements"].toString())


        //Check that the uses of a specific shortened URL have been updated
        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/user.click.hash?tag=hash:4392f73f",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        actualObj= mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"COUNT\",\"value\":1.0}]", actualObj["measurements"].toString())


        /*
        //Check it has registered an access for a qr
        response4 = restTemplate.getForEntity(
            "http://localhost:$port/metrics/user.action?tag=type:qrUsed",
            String::class.java)
        assertEquals(HttpStatus.OK, response4.statusCode)

        actualObj= mapper.readTree(response4.body)

        assertNotNull(actualObj)
        assertEquals("[{\"statistic\":\"COUNT\",\"value\":1.0}]", actualObj["measurements"].toString())
        */


    }



}