package es.unizar.urlshortener

import com.google.common.collect.Sets
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
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
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(getInfo())
                .useDefaultResponseMessages(false)
    }

    private fun getInfo(): ApiInfo {
        return ApiInfoBuilder()
                .title("URL shortener API description")
                .description("URL shortener project for UNIZAR subject Web Engineering.")
                .version("0.0.1")
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .contact(Contact("Contact", "https://github.com/Feer93/urlshortener", "774840@unizar.es"))
                .build()
    }
}