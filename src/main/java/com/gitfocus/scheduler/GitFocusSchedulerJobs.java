package com.gitfocus.scheduler;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gitfocus.git.db.impl.CommitDetailGitServiceImpl;
import com.gitfocus.git.db.impl.PullCommitGitServiceImpl;
import com.gitfocus.git.db.impl.PullMasterGitServiceImpl;
import com.gitfocus.git.db.impl.ReviewDetailsGitServiceImpl;
import com.gitfocus.repository.CommitDetailsRepository;
import com.gitfocus.repository.PullMasterRepository;
import com.gitfocus.repository.ReviewDetailsRepository;

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

	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
	@Autowired
	CommitDetailGitServiceImpl commitDetailsGitService;
	@Autowired
	PullCommitGitServiceImpl pullCommitGitService;
	@Autowired
	PullMasterGitServiceImpl pullMasterGitService;
	@Autowired
	ReviewDetailsGitServiceImpl reviewDetailsGitService;
	@Autowired
	CommitDetailsRepository commitRepository;	
	@Autowired
	PullMasterRepository pullMasterRepository;	
	@Autowired
	ReviewDetailsRepository reviewDetailsRepository;	

	// Run scheduler after application start-up
	@PostConstruct
	public void onStartup() throws Exception {
		scheduledTaskForGitCommitDetailService();
	}

	// Run scheduler @ 12AM on every day
	@Scheduled(cron="0 0 0 * * ?")
	public void onSchedule() throws Exception {
		scheduledTaskForGitCommitDetailService();
		scheduledTaskForGitPullMasterService();
		scheduledTaskForGitPullCommitService();
		scheduledTaskForGitReviewDetailService();
	}

	public void scheduledTaskForGitCommitDetailService() throws Exception {
		logger.debug("CommitDetailGitServiceImpl Scheduler Task is Running ..... ");
		Timestamp startDate = (Timestamp) commitRepository.getLastCommitDate();
		logger.debug("CommitDetailGitServiceImpl Scheduler Task StartDate and EndDate " + startDate +  LocalDate.now());
		commitDetailsGitService.commitDetailsSchedulerJob(startDate, LocalDateTime.now());
		logger.info("CommitDetailGitServiceImpl Scheduler Completed ...!");
	}

	public void scheduledTaskForGitPullMasterService() throws Exception {
		logger.debug("PullMasterGitServiceImpl Scheduler Task is Running ..... ");
		Timestamp startDate = (Timestamp) pullMasterRepository.getLastPRCreatedDate();
		logger.debug("PullMasterGitServiceImpl Scheduler Task StartDate and EndDate " + startDate +  LocalDate.now());
		pullMasterGitService.pullMasterSchedulerJob(startDate, LocalDateTime.now());
		logger.info("PullMasterGitServiceImpl Scheduler Completed ...!");
	}

	public void scheduledTaskForGitPullCommitService() throws Exception {
		logger.debug("PullCommitGitServiceImpl Scheduler Task is Running ..... ");
		LocalDateTime endDate = LocalDateTime.now();
		LocalDateTime startDate = endDate.plusDays(-1);
		logger.debug("PullCommitGitServiceImpl Scheduler Task StartDate and EndDate " + startDate +  endDate);
		pullCommitGitService.pullCommitSchedulerJob(startDate, LocalDateTime.now());
		logger.info("PullCommitGitServiceImpl Scheduler Completed ...!");
	}

	public void scheduledTaskForGitReviewDetailService() throws Exception {
		logger.debug("ReviewDetailGitServiceImpl Scheduler Task is Running ..... ");
		Timestamp startDate = (Timestamp) reviewDetailsRepository.getLastReviewDate();
		logger.debug("ReviewDetailGitServiceImpl Scheduler Task StartDate and EndDate " + startDate +  LocalDate.now());
		reviewDetailsGitService.reviewDetailsSchedulerJob(startDate, LocalDateTime.now());
		logger.info("ReviewDetailGitServiceImpl Scheduler Completed ...!");
	}

}
