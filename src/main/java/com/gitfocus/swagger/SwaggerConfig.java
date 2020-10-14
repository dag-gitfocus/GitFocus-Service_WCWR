package com.gitfocus.swagger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * 
 * @author Tech Mahindra
 * Swagger config file which is display basic info on Swagger UI
 * API format output for REST Services 
 * Swagger URL - http://localhost:8888/swagger-ui/index.html
 */


@Configuration
public class SwaggerConfig {

	public static final Contact DEFAULT_CONTACT = new Contact(
			"GitFocus", "http://www.GitFocus.com", "gitfocus@gmail.com");

	public static final ApiInfo DEFAULT_API_INFO = new ApiInfo(
			"GitFocus", "GitFoucs Product", "2.0",
			"urn:tos", DEFAULT_CONTACT, 
			"Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0", Arrays.asList());

	private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = 
			new HashSet<String>(Arrays.asList("application/json",
					"application/xml"));

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(DEFAULT_API_INFO)
				.produces(DEFAULT_PRODUCES_AND_CONSUMES)
				.consumes(DEFAULT_PRODUCES_AND_CONSUMES);
	}
}