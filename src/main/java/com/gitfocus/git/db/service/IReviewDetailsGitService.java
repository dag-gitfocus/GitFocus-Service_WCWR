package com.gitfocus.git.db.service;

import java.text.ParseException;

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
	public void reviewDetailsSchedulerJob();

}
