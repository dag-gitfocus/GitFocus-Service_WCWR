package com.gitfocus.constants;

import org.springframework.stereotype.Service;

/**
 * @author Tech Mahindra 
 * Constant class for GitFocus-Service Application
 */
@Service
public class GitFocusConstants {

    public final String BASE_URI = "https://api.github.com/repos/";
    public final String ACCESS_TOKEN = "access_token=";
    public final int TOTAL_RECORDS_PER_PAGE= 100;
    public final int MAX_PAGE = 30;
}
