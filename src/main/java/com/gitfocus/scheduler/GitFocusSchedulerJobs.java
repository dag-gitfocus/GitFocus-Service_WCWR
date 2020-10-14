package com.gitfocus.scheduler;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gitfocus.git.db.service.IBranchDetailGitService;
import com.gitfocus.git.db.service.ICommitDetailGitService;
import com.gitfocus.git.db.service.IPullCommitGitService;
import com.gitfocus.git.db.service.IPullMasterGitService;
import com.gitfocus.git.db.service.IReviewDetailsGitService;

/**
 * 
 * @author Tech Mahindra
 * Swagger config file which is diplay basic info on Swagger UI
 */

@Component
public class GitFocusSchedulerJobs {

	private static final Logger logger = LogManager.getLogger(GitFocusSchedulerJobs.class.getSimpleName());

	public GitFocusSchedulerJobs() {
		super();
		logger.info("GitFocus ScheduledTasks init...");
	}

	@Autowired
	IBranchDetailGitService branchDetailService;
	@Autowired
	ICommitDetailGitService commitDetailsGitService;
	@Autowired
	IPullCommitGitService pullCommitGitService;
	@Autowired
	IPullMasterGitService pullMasterGitService;
	@Autowired
	IReviewDetailsGitService reviewDetailsGitService;

	// Run scheduler on application start-up
	@PostConstruct
	public synchronized void onStartup() throws Exception {
//		branchDetailService.branchDetailsSchedulerJob();
//		commitDetailsGitService.commitDetailsSchedulerJob();
//		pullMasterGitService.pullMasterSchedulerJob();
//		pullCommitGitService.pullCommitSchedulerJob();
//		reviewDetailsGitService.reviewDetailsSchedulerJob();
	}

	/* ==========================================================================

	 These are valid formats for cron expressions:

		0 0 * * * * = the top of every hour of every day.
		10 * * * * * = every ten seconds.
		0 0 8-10 * * * = 8, 9 and 10 o'clock of every day.
		0 0 6,19 * * * = 6:00 AM and 7:00 PM every day.
		0 0/30 8-10 * * * = 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day.
		0 0 9-17 * * MON-FRI = on the hour nine-to-five weekdays
		0 0 0 25 12 ? = every Christmas Day at midnight
		The pattern is:
		second, minute, hour, day, month, weekday 

	 ========================================================================== */

	// Run scheduler @ 12AM on every day
	@Scheduled(cron = "0 0 0 * * *",zone = "Asia/Kolkata")
	public synchronized void onSchedule() throws Exception {
//		branchDetailService.branchDetailsSchedulerJob();
//		commitDetailsGitService.commitDetailsSchedulerJob();
//		pullMasterGitService.pullMasterSchedulerJob();
//		pullCommitGitService.pullCommitSchedulerJob();
//		reviewDetailsGitService.reviewDetailsSchedulerJob();
	}
}