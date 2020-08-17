package com.gitfocus.constants;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

/**
 * @author Tech Mahindra 
 * Constant class for GitFocus-Service Application
 */
@Service
public class GitFocusConstants {

    public final String BASE_URI = "https://api.github.com/repos/";
    public final String ACCESS_TOKEN = "access_token=";
    public final int MAX_PAGE = 15;
    public final int TOTAL_RECORDS_PER_PAGE= 100;
    public final int SCHEDULER_MAX_PAGE = 2;
    public final int SCHEDULER_TOTAL_RECORDS_PER_PAGE= 100;
    public final LocalDate ENDDATE = LocalDate.now();
    public final LocalDate STARTDATE = ENDDATE.plusDays(-90);
}
