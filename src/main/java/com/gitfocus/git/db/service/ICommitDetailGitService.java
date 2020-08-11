package com.gitfocus.git.db.service;

import java.text.ParseException;

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
    public void commitDetailsSchedulerJob() throws ParseException;

}
