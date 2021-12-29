package swagger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class SwaggerApplication {

    fun main(args:Array<String>) {
        SpringApplication.run(SwaggerApplication::class.java, *args)
    }
}
