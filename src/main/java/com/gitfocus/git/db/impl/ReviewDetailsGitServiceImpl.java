package com.gitfocus.git.db.impl;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gitfocus.constants.GitFocusConstants;
import com.gitfocus.git.db.model.ReviewDetails;
import com.gitfocus.git.db.model.ReviewDetailsCompositeId;
import com.gitfocus.git.db.model.Units;
import com.gitfocus.git.db.service.IReviewDetailsGitService;
import com.gitfocus.repository.BranchDetailsRepository;
import com.gitfocus.repository.GitFocusSchedulerRepository;
import com.gitfocus.repository.PullMasterRepository;
import com.gitfocus.repository.ReviewDetailsRepository;
import com.gitfocus.repository.UnitReposRepository;
import com.gitfocus.repository.UnitsRepository;
import com.gitfocus.util.GitFocusUtil;

/**
 * @author Tech Mahindra 
 * Service class for ReviewDetails and store values in review_details table in DB
 * 
 * NOTE : By default GitAPIJson gives max 30 records only for each RestAPI call but for some API have more than 30 records, hence we to
 * have to append page number and totalNoOfRecords/perPage for each URL's to fetch rest of the records
 * 
 */
@Service
public class ReviewDetailsGitServiceImpl implements IReviewDetailsGitService {

	private static final Logger logger = LogManager.getLogger(ReviewDetailsGitServiceImpl.class.getSimpleName());

	public ReviewDetailsGitServiceImpl() {
		super();
		logger.info("ReviewDetailsGitServiceImpl init");
	}

	@Autowired
	private GitFocusConstants gitFocusConstant;
	@Autowired
	private ReviewDetailsRepository reviewRepo;
	@Autowired
	private UnitsRepository unitsRepository;
	@Autowired
	private UnitReposRepository uReposRepository;
	@Autowired
	GitFocusUtil gitUtil;
	@Autowired
	private PullMasterRepository pullMasterRepo;
	@Autowired 
	GitFocusSchedulerRepository gitFocusSchedulerRepo;
	@Autowired
	private BranchDetailsRepository branchRepo;

	int reviewId = 0;
	String reviewResults = null;
	String reviewedBy = null;
	String reviewComment = null;
	String state = null;
	String reviewedAt = null;
	String commitId = null;
	int unitId = 0;
	JSONObject reviewObjUser = null;
	JSONArray jsonResponse = null;
	String reviewURI = null;
	boolean result = false;
	String user = null;
	String accessToken = null;
	int repoId = 0;
	JSONObject reviewObj = null;
	List<String> pullNoList = null;
	String unitOwner = null;
	List<String> reposName = null;
	List<String> branches = null;
	String errorMessage = null;
	LocalDateTime localDateTime = LocalDateTime.now();
	Date serviceExecTime = null;
	ReviewDetails rDetails = new ReviewDetails();
	ReviewDetailsCompositeId rDetailsCompositeId = new ReviewDetailsCompositeId();

	@Override
	public boolean save() throws ParseException {
		// TODO Auto-generated method stub

		List<Units> units = (List<Units>) unitsRepository.findAll();
		if (units.isEmpty()) {
			return result;
		}
		units.forEach(response -> {
			unitId = response.getUnitId();
			unitOwner = response.getUnitOwner();
			reposName = uReposRepository.findReposName(unitId);

			reposName.forEach(repoName -> {
				repoId = uReposRepository.findRepoId(repoName);
				pullNoList = pullMasterRepo.findPullNo(repoId);

				pullNoList.forEach(reviewNo -> {
					for (int page = 1; page <= gitFocusConstant.MAX_PAGE; page++) {

						// To get review details based on all the pull history
						reviewURI =  gitFocusConstant.BASE_URI + unitOwner + "/" + repoName
								+ "/pulls/"+reviewNo+"/reviews?"+"state=all"+"&" + "since="+ gitFocusConstant.STARTDATE + "&"+ "until=" + gitFocusConstant.ENDDATE + "page=" + page  + "&per_page=" + gitFocusConstant.TOTAL_RECORDS_PER_PAGE+ "&";
						reviewResults = gitUtil.getGitAPIJsonResponse(reviewURI);

						try {
							jsonResponse = new JSONArray(reviewResults);
							for (int i = 0; i < jsonResponse.length(); i++) {
								reviewObj = jsonResponse.getJSONObject(i);
								reviewObjUser = reviewObj.getJSONObject("user");
								reviewId = reviewObj.getInt("id");
								reviewedBy = reviewObjUser.getString("login");
								reviewComment = reviewObj.getString("body");
								state = reviewObj.getString("state");
								reviewedAt = reviewObj.getString("submitted_at");
								commitId = reviewObj.getString("commit_id");

								rDetailsCompositeId.setReviewId(reviewId);
								rDetails.setrDetailCompositeId(rDetailsCompositeId);

								rDetails.setUnitId(unitId);
								rDetails.setRepoId(repoId);
								rDetails.setPullNumber(Integer.parseInt(reviewNo));
								rDetails.setReviewedBy(reviewedBy);
								rDetails.setReviewComment(reviewComment);
								rDetails.setState(state);
								rDetails.setReviewedAt(GitFocusUtil.stringToDate(reviewedAt));
								rDetails.setCommitId(commitId);

								reviewRepo.save(rDetails);

								logger.info("Records saved in review_details table in DB");
							}

						} catch (JSONException e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				});
			});
		});
		return true;
	}

	/**
	 * Method to execute scheduler jobs for PR Review details
	 */
	@Override
	public void reviewDetailsSchedulerJob() {
		// TODO Auto-generated method stub
		List<Units> units = (List<Units>) unitsRepository.findAll();
		units.forEach(response -> {
			unitId = response.getUnitId();
			unitOwner = response.getUnitOwner();
			reposName = uReposRepository.findReposName(unitId);

			reposName.forEach(repoName -> {
				repoId = uReposRepository.findRepoId(repoName);

				// get branches for repository
				branches = branchRepo.getBranchList(repoId);

				branches.forEach(branchName -> {
					reviewDetailsSchedulerJobToSaveRecordsInDB(repoName, branchName, unitId, repoId);
				});
			});
		});
	}

	/**
	 * Method to save the values in commit_details table in DB through scheduler
	 * Capture scheduler events and log to gitservice_scheduler_status tables DB table
	 * 
	 * @param repoName
	 * @param unitId
	 * @param repoId
	 */
	private void reviewDetailsSchedulerJobToSaveRecordsInDB(String repoName, String branchName, int unitId, int repoId) {
		// TODO Auto-generated method stub
		logger.info("reviewDetailsSchedulerJobToSaveRecordsInDB()" + repoId + repoName);
		String serviceName = "ReviewDetails";
		String status;
		int pullNumber;

		//get the last scheduler status for each repository and branch whether its success or failure
		status = gitFocusSchedulerRepo.getSeriveStatusForPullCommit(repoName, serviceName);

		// getting records first time from table gitservice_scheduler_status might be null in status column
		// if service status success then get last pull number 
		if(status == null || status.equalsIgnoreCase("success")) {
			// get the last pull_number from review_details table
			pullNumber = reviewRepo.getlastSuccessfulPullNumber(repoId);
			pullNoList = pullMasterRepo.findPullNoAfterLastSucceessfulRun(pullNumber);
		}
		// if service status failure then get last failure pull_number
		else if (status.equalsIgnoreCase("failure")) {
			// get the last failure pull_number
			pullNumber = reviewRepo.getlastFailurePullNumber(repoId);
			pullNoList = pullMasterRepo.findPullNoFromLastFailureRun(pullNumber);
		}
		try {
			pullNoList.forEach(reviewNo -> {
				for (int page = 1; page <= gitFocusConstant.SCHEDULER_MAX_PAGE; page++) {

					// To get review details based on all the pull history
					reviewURI =  gitFocusConstant.BASE_URI + unitOwner + "/" + repoName
							+ "/pulls/"+reviewNo+"/reviews?"+"state=all"+"&" + "page=" + page  + "&per_page=" + gitFocusConstant.SCHEDULER_TOTAL_RECORDS_PER_PAGE+ "&";
					reviewResults = gitUtil.getGitAPIJsonResponse(reviewURI);

					jsonResponse = new JSONArray(reviewResults);
					for (int i = 0; i < jsonResponse.length(); i++) {
						reviewObj = jsonResponse.getJSONObject(i);
						reviewObjUser = reviewObj.getJSONObject("user");
						reviewId = reviewObj.getInt("id");
						reviewedBy = reviewObjUser.getString("login");
						reviewComment = reviewObj.getString("body");
						state = reviewObj.getString("state");
						reviewedAt = reviewObj.getString("submitted_at");
						commitId = reviewObj.getString("commit_id");

						rDetailsCompositeId.setReviewId(reviewId);
						rDetails.setrDetailCompositeId(rDetailsCompositeId);

						rDetails.setUnitId(unitId);
						rDetails.setRepoId(repoId);
						rDetails.setPullNumber(Integer.parseInt(reviewNo));
						rDetails.setReviewedBy(reviewedBy);
						rDetails.setReviewComment(reviewComment);
						rDetails.setState(state);
						rDetails.setReviewedAt(GitFocusUtil.stringToDate(reviewedAt));
						rDetails.setCommitId(commitId);

						reviewRepo.save(rDetails);

						logger.info("Records saved in review_details table in DB --  through scheduler ");
					}

				}
			});
		} catch (JSONException e) {
			// TODO: handle exception
			errorMessage = e.getMessage();
			e.printStackTrace();
		}
		// Scheduler events to save in DB table
		if (pullNoList != null && !pullNoList.isEmpty() && errorMessage == null) {
			// has some PR details and scheduler job status is success
			logger.info("reviewDetailsSchedulerJobToSaveRecordsInDB() scheduler status is success");
			String serviceStatus = "success";
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			String errorMessage = "";
			// capture and save scheduler status in gitservice_scheduler_status table in DB for successful scheduler job
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);
		} if (pullNoList == null || pullNoList.isEmpty()) {
			// sometimes may not have PR details records for particular time period
			// consider this scenario is success but there is no records
			logger.info("reviewDetailsSchedulerJobToSaveRecordsInDB() may not have pull commit details on " + LocalDate.now());
			String serviceStatus = "success";
			String errorMessage = "Sceduler completed Job but there is no PULL commit details records on" + LocalDate.now();
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			// capture and save scheduler status in gitservice_scheduler_status table for there is no commit details
			// record for particular time period
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);
		}
		if (errorMessage != null) {
			// has some exception while running scheduler 
			logger.info("reviewDetailsSchedulerJobToSaveRecordsInDB() scheduler status failure");
			String serviceStatus = "failure";
			LocalDateTime localDateTime = LocalDateTime.now();
			Date serviceExecTime = Date.from(localDateTime.atZone( ZoneId.systemDefault()).toInstant());
			// log exception details in gitservice_scheduler_status table in DB
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);
		}
	}
}
