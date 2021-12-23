package es.unizar.urlshortener.infrastructure.delivery

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Controller("Pages Controller")
class PagesController {

    @RequestMapping("/errorp")
    fun ErrorPage(): String {
        return "error.html"
    }
}