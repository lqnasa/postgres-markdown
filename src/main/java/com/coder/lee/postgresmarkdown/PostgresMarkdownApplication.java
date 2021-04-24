package com.coder.lee.postgresmarkdown;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import springfox.documentation.oas.annotations.EnableOpenApi;

@EnableOpenApi
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PostgresMarkdownApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgresMarkdownApplication.class, args);
    }

}
