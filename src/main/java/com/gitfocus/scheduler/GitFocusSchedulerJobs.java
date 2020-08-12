package com.gitfocus.scheduler;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gitfocus.git.db.impl.PullCommitGitServiceImpl;
import com.gitfocus.git.db.impl.PullMasterGitServiceImpl;
import com.gitfocus.git.db.impl.ReviewDetailsGitServiceImpl;
import com.gitfocus.git.db.service.ICommitDetailGitService;

/**
 * 
 * @author Tech Mahindra
 * Scheduler class for Commit/PullRequest/PR Review for Git Services 
 */

@Component
public class GitFocusSchedulerJobs {

	private static final Logger logger = LogManager.getLogger(GitFocusSchedulerJobs.class.getSimpleName());

	public GitFocusSchedulerJobs() {
		super();
		logger.info("GitFocus ScheduledTasks init...");
	}

	@Autowired
	ICommitDetailGitService commitDetailsGitService;
	@Autowired
	PullCommitGitServiceImpl pullCommitGitService;
	@Autowired
	PullMasterGitServiceImpl pullMasterGitService;
	@Autowired
	ReviewDetailsGitServiceImpl reviewDetailsGitService;

	// Run scheduler after application start-up
	@PostConstruct
	public synchronized void onStartup() throws Exception {
//		commitDetailsGitService.commitDetailsSchedulerJob();
		pullMasterGitService.pullMasterSchedulerJob();
	}

	// Run scheduler @ 12AM on every day
	@Scheduled(cron="0 0 0 * * ?")
	public synchronized void onSchedule() throws Exception {
		commitDetailsGitService.commitDetailsSchedulerJob();
		pullMasterGitService.pullMasterSchedulerJob();
		pullCommitGitService.pullCommitSchedulerJob();
		reviewDetailsGitService.reviewDetailsSchedulerJob();
	}
}