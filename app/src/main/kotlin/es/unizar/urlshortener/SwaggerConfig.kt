package es.unizar.urlshortener

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.swagger2.annotations.EnableSwagger2


/**
 * Swagger documentation interface available at:
 * http://localhost:8080/swagger-ui.html
 * Swagger metrics as a JSON file available at:
 * http://localhost:8080/api-docs
 * Swagger metrics as a YAML file available at (this will download a yaml file to your computer):
 * http://localhost:8080/api-docs.yaml
 */
@Configuration
@EnableSwagger2
class SwaggerConf {

    @Bean
    fun urlShortenerOpenAPI(): OpenAPI? {
        return OpenAPI()
                .info(Info().title("URL Shortener API ")
                .contact(Contact()
                        .email("774840@unizar.es")
                        .url("https://github.com/Feer93")
                        .name("Fernando"))
                .description("URL shortener project for UNIZAR subject Web Engineering by Group A.")
                .version("v1.0.0")
                .license(License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(ExternalDocumentation()
                .url("https://github.com/Feer93/urlshortener").description("Project repository on Github"))
    }
}