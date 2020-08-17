package com.gitfocus.git.db.service;

import java.text.ParseException;

/**
 * @author Tech Mahindra
 * 
 */
public interface IBranchDetailGitService {

	/**
	 * 
	 * @return boolean
	 */
	public boolean save();

	/**
	 * 
	 * @return boolean
	 * @throws ParseException
	 *
	 */
	public void branchDetailsSchedulerJob() throws ParseException;

}
