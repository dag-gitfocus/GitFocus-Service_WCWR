package com.gitfocus.git.db.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * @author Tech Mahindra
 * 
 */
public interface ICommitDetailGitService {

    /**
     * 
     * @param serviceName 
     * @return boolean
     * @throws ParseException
     *
     */
    public boolean save() throws ParseException;
    
    /**
     * 
     * @return boolean
     * @throws ParseException
     *
     */
    public boolean commitDetailsSchedulerJob(Timestamp startDate, LocalDateTime endDate) throws ParseException;

}
