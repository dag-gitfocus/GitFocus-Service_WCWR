package com.gitfocus.git.db.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * @author Tech Mahindra
 *
 */
public interface IPullMasterGitService {

    /**
     * 
     * @return boolean
     * @throws ParseException
     * 
     */
    public boolean save();

    /**
     * 
     * @param startDate
     * @param endDate
     * @return
     */
	boolean pullMasterSchedulerJob(Timestamp startDate, LocalDateTime endDate);
}