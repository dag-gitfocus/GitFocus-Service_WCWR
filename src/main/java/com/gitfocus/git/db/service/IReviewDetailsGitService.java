package com.gitfocus.git.db.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * @author Tech Mahindra
 *
 */
public interface IReviewDetailsGitService {
	
	/**
	 * 
	 * @return
	 * @throws ParseException
	 */
	public boolean save() throws ParseException;

	/**
	 * 
	 * @param startDate
	 * @param now
	 * @return
	 */
	boolean reviewDetailsSchedulerJob(Timestamp startDate, LocalDateTime endDate);

}
