package com.gitfocus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Tech Mahindra 
 * Initial class to boot GitFocus Application
 */
@SpringBootApplication
public class GitFocusApplication {

	private static final Logger logger = LogManager.getLogger(GitFocusApplication.class.getSimpleName());

	public GitFocusApplication() {
		super();
		// TODO Auto-generated constructor stub
		logger.info("Starting GitFocus-Service Application..");
	}

	public static void main(String[] args) {
		SpringApplication.run(GitFocusApplication.class, args);
	}
}