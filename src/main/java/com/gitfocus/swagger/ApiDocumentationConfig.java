package com.gitfocus.swagger;

import io.swagger.annotations.Contact;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;


/**
 * 
 * @author Tech Mahindra
 * Swagger config file which is diplay basic info on Swagger UI
 */

@SwaggerDefinition(
		info = @Info(
				description = "GitFocus",
				version = "V1.0",
				title = "GitFocus API",
				contact = @Contact(
						name = "gitfocus", 
						email = "gitfocus@gmail.com", 
						url = "http://www.gitfocus.com"
						),
				license = @License(
						name = "Apache 2.0", 
						url = "http://www.apache.org/licenses/LICENSE-2.0"
						)
				),
		consumes = {"application/json", "application/xml"},
		produces = {"application/json", "application/xml"},
		schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
		externalDocs = @ExternalDocs(value = "Read This For Sure", url = "http://www.gitfocus.com")
		)
public interface ApiDocumentationConfig {

}
