package com.gitfocus.git.db.service;

import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * @author Tech Mahindra
 *
 */
public interface IPullCommitGitService {
	/**
	 * 
	 * @return boolean
	 * @throws ParseException
	 */
	public boolean save() throws ParseException;

	/**
	 * 
	 * @param startDate
	 * @param now
	 * @return
	 */
	boolean pullCommitSchedulerJob(LocalDateTime startDate, LocalDateTime endDate);

}
