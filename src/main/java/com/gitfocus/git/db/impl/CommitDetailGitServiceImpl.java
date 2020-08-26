package com.gitfocus.git.db.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gitfocus.constants.GitFocusConstants;
import com.gitfocus.git.db.model.CommitDetails;
import com.gitfocus.git.db.model.CommitDetailsCompositeId;
import com.gitfocus.git.db.model.Units;
import com.gitfocus.git.db.service.ICommitDetailGitService;
import com.gitfocus.repository.BranchDetailsRepository;
import com.gitfocus.repository.CommitDetailsRepository;
import com.gitfocus.repository.GitFocusSchedulerRepository;
import com.gitfocus.repository.TeamMembersRepository;
import com.gitfocus.repository.UnitReposRepository;
import com.gitfocus.repository.UnitsRepository;
import com.gitfocus.util.GitFocusUtil;

/**
 * @author Tech Mahindra 
 * Service class for CommitDetails and store values in commit_details table in DB
 * 
 * NOTE : By default GitAPIJson gives max 30 records only for each RestAPI call but for some API have more than 30 records, hence we to
 * append page number and totalNoOfRecords/perPage for each URL's to fetch rest of the records
 * 
 */
@Service
public class CommitDetailGitServiceImpl implements ICommitDetailGitService {

	private static final Logger logger = LogManager.getLogger(CommitDetailGitServiceImpl.class.getSimpleName());

	public CommitDetailGitServiceImpl() {
		super();
		logger.info("CommitDetailServiceImpl init");
	}

	@Autowired
	private GitFocusConstants gitConstant;
	@Autowired
	private UnitReposRepository uReposRepository;
	@Autowired
	private UnitsRepository uRepository;
	@Autowired
	private CommitDetailsRepository cDetailsRepository;
	@Autowired
	private BranchDetailsRepository bDetailsRepository;
	@Autowired
	GitFocusUtil gitUtil;
	@Autowired
	CommitDetailsRepository commitRepository;
	@Autowired 
	GitFocusSchedulerRepository gitFocusSchedulerRepo;
	@Autowired
	TeamMembersRepository teamMemRepos;

	List<Units> unitRecords = null;
	List<Units> units = null;
	String jsonResult = null;
	String unitOwner = null;
	int unitId = 0;
	int repoId = 0;
	String commitsResult = null;
	String shaId = null;
	String commitsUri = null;
	JSONArray jsonResponse = null;
	JSONObject commitDetailObj = null;
	JSONObject commitObj1 = null;
	JSONObject commitObj2 = null;
	JSONObject commitObj3 = null;
	String userId = null;
	String commitDetailURI = null;
	String commitDetailShaURI = null;
	String commitDetailShaResult = null;
	JSONObject commitDetailShaObj = null;
	JSONArray commitShaArr = null;
	JSONObject jsonObj = null;
	JSONObject jsonShaObj = null;
	Date cDate = null;
	String commitDate = null;
	String message = null;
	List<String> reposName = null;
	List<String> branches = null;
	boolean schedulerResult = false;
	String errorMessage = null;
	Timestamp startDate = null;
	LocalDateTime endDate = null;
	LocalDateTime localDateTime = LocalDateTime.now();
	Date serviceExecTime = null;
	CommitDetails cDetails = new CommitDetails();
	CommitDetailsCompositeId commitCompositeId = new CommitDetailsCompositeId();

	/**
	 * Method to get Json from Git and store values in DataBase
	 * @return boolean
	 * @throws ParseException
	 */
	@Override
	public boolean save() {
		logger.info("CommitDetailServiceImpl save()");
		boolean result = false;
		units = (List<Units>) uRepository.findAll();
		if (units.isEmpty()) {
			return result;
		}
		units.forEach(response -> {
			unitId = response.getUnitId();
			unitOwner = response.getUnitOwner();
			reposName = uReposRepository.findReposName(unitId);

			reposName.forEach(repoName -> {
				repoId = uReposRepository.findRepoId(repoName);

				// get branches for repository
				branches = bDetailsRepository.getBranchList(repoId);

				branches.forEach(branchName -> {
					for (int page = 1; page <= gitConstant.MAX_PAGE; page++) {
						commitDetailURI = gitConstant.BASE_URI + unitOwner + "/" + repoName + "/commits?" + "sha="
								+ branchName + "&"+ "since="+ gitConstant.STARTDATE + "&"+ "until=" + gitConstant.ENDDATE + "&page=" + page + "&" + "per_page=" + gitConstant.TOTAL_RECORDS_PER_PAGE + "&";

						commitsResult = gitUtil.getGitAPIJsonResponse(commitDetailURI);
						jsonResponse = new JSONArray(commitsResult);

						for (int i = 0; i < jsonResponse.length(); i++) {
							commitDetailObj = jsonResponse.getJSONObject(i);

							commitObj1 = commitDetailObj.getJSONObject("commit");
							commitObj2 = commitObj1.getJSONObject("author");
							if (commitDetailObj.has("author") && !commitDetailObj.isNull("author")) {
								commitObj3 = commitDetailObj.getJSONObject("author");
								userId = commitObj3.getString("login");
							}

							shaId = commitDetailObj.getString("sha");
							commitDate = commitObj2.getString("date");
							cDate = GitFocusUtil.stringToDate(commitDate);
							message = commitObj1.getString("message");

							// commit_detail based on sha_id -- START
							if (shaId != null) {

								commitDetailShaURI = gitConstant.BASE_URI + unitOwner + "/" + repoName + "/commits/" + shaId + "?";

								commitDetailShaResult = gitUtil.getGitAPIJsonResponse(commitDetailShaURI);
								commitDetailShaObj = new JSONObject(commitDetailShaResult);
								commitShaArr = commitDetailShaObj.getJSONArray("files");

								String fileName = null;
								String fileStatus = null;
								String linesAdded = null;
								String linesRemoved = null;

								for (int j = 0; j < commitShaArr.length(); j++) {
									jsonShaObj = commitShaArr.getJSONObject(j);
									fileName = fileName + jsonShaObj.getString("filename").concat(",");
									fileStatus = fileStatus + jsonShaObj.getString("status").concat(",");
									linesAdded = linesAdded	+ String.valueOf(jsonShaObj.getInt("additions")).concat(",");
									linesRemoved = linesRemoved	+ String.valueOf(jsonShaObj.getInt("deletions")).concat(",");

									// commit_detail based on sha_id -- END

									// store values in commit_details table in database
									commitCompositeId.setUnitId(unitId);
									commitCompositeId.setShaId(shaId);
									commitCompositeId.setRepoId(repoId);
									commitCompositeId.setBranchName(branchName);

									cDetails.setcCompositeId(commitCompositeId);

									cDetails.setCommitDate(cDate);
									cDetails.setUserId(userId);
									cDetails.setMessage(message);
									cDetails.setFileName(fileName.replace("null", ""));
									cDetails.setFileStatus(fileStatus.replace("null", ""));
									cDetails.setLinesAdded(linesAdded.replace("null", ""));
									cDetails.setLinesRemoved(linesRemoved.replace("null", ""));

									cDetailsRepository.save(cDetails);

									logger.info("Records saved in commit_details table in DB");
								}
							}
						}
					}
				});
			});
		});
		return true;
	}

	/**
	 * Method to execute scheduler jobs for commit details
	 * Get the repository and branch
	 */
	@Override
	public void commitDetailsSchedulerJob() throws ParseException {
		// TODO Auto-generated method stub

		logger.info("commitDetailsSchedulerJob save()");

		units = (List<Units>) uRepository.findAll();
		units.forEach(response -> {
			unitId = response.getUnitId();
			unitOwner = response.getUnitOwner();
			reposName = uReposRepository.findReposName(unitId);

			reposName.forEach(repoName -> {
				repoId = uReposRepository.findRepoId(repoName);

				// get branches for repository
				branches = bDetailsRepository.getBranchList(repoId);

				branches.forEach(branchName -> {
					commitDetailsSchedulerJobToSaveRecordsInDB(repoName, branchName, unitId);

				});
			});
		});
	}

	/**
	 * Method to save the values in commit_details table in DB through scheduler
	 * Capture scheduler events and log to gitservice_scheduler_status tables DB table
	 * 
	 * @param repoName
	 * @param branchName
	 */
	private void commitDetailsSchedulerJobToSaveRecordsInDB(String repoName, String branchName, int unitId) {
		// TODO Auto-generated method stub
		logger.info("commitDetailsSchedulerJobToSaveRecordsInDB()" + repoName + branchName);
		String serviceName = "CommitDetail";
		String serviceStatus = null;
		
		//get the last scheduler status for each repository and branch whether its success or failure
		serviceStatus = gitFocusSchedulerRepo.getSeriveStatus(repoName, branchName, serviceName);
		repoId = uReposRepository.findRepoId(repoName);

		// getting records first time from table gitservice_scheduler_status might be null in status column
		// if service status success then fetch last commit date for each repository and branch 
		if(serviceStatus == null || serviceStatus.equalsIgnoreCase("success")) {
			startDate = commitRepository.getLastSuccessfulCommitDate(repoId, branchName);
			endDate = LocalDateTime.now();
		}
		// if service status failure then fetch last scheduler exec time for failed repository and branch
		else if (serviceStatus.equalsIgnoreCase("failure")) {
			// get the last commit details scheduler status for failed repository and branch
			startDate = gitFocusSchedulerRepo.getLastExecTime(repoName, branchName, serviceName);
			endDate = LocalDateTime.now();
		}
		// hit the git and get json response based on success or failure for commit details service
		try {
			for (int page = 1; page <= gitConstant.SCHEDULER_MAX_PAGE; page++) {
				commitDetailURI = gitConstant.BASE_URI + unitOwner + "/" + repoName + "/commits?" + "sha="
						+ branchName + "&"+ "since="+ startDate + "&"+ "until=" + endDate + "&page=" + page + "&" + "per_page=" + gitConstant.SCHEDULER_TOTAL_RECORDS_PER_PAGE + "&";

				commitsResult = gitUtil.getGitAPIJsonResponse(commitDetailURI);
				jsonResponse = new JSONArray(commitsResult);

				for (int i = 0; i < jsonResponse.length(); i++) {
					commitDetailObj = jsonResponse.getJSONObject(i);

					commitObj1 = commitDetailObj.getJSONObject("commit");
					commitObj2 = commitObj1.getJSONObject("author");
					if (commitDetailObj.has("author") && !commitDetailObj.isNull("author")) {
						commitObj3 = commitDetailObj.getJSONObject("author");
						userId = commitObj3.getString("login");
					}

					shaId = commitDetailObj.getString("sha");
					commitDate = commitObj2.getString("date");
					cDate = GitFocusUtil.stringToDate(commitDate);
					message = commitObj1.getString("message");

					if (shaId != null) {
						commitDetailShaURI = gitConstant.BASE_URI + unitOwner + "/" + repoName + "/commits/" + shaId + "?";
						commitDetailShaResult = gitUtil.getGitAPIJsonResponse(commitDetailShaURI);
						commitDetailShaObj = new JSONObject(commitDetailShaResult);
						commitShaArr = commitDetailShaObj.getJSONArray("files");

						String fileName = null;
						String fileStatus = null;
						String linesAdded = null;
						String linesRemoved = null;

						for (int j = 0; j < commitShaArr.length(); j++) {
							jsonShaObj = commitShaArr.getJSONObject(j);
							fileName = fileName + jsonShaObj.getString("filename").concat(",");
							fileStatus = fileStatus + jsonShaObj.getString("status").concat(",");
							linesAdded = linesAdded	+ String.valueOf(jsonShaObj.getInt("additions")).concat(",");
							linesRemoved = linesRemoved	+ String.valueOf(jsonShaObj.getInt("deletions")).concat(",");

							// store values in commit_details table in database
							commitCompositeId.setUnitId(unitId);
							commitCompositeId.setShaId(shaId);
							commitCompositeId.setRepoId(repoId);
							commitCompositeId.setBranchName(branchName);

							cDetails.setcCompositeId(commitCompositeId);

							cDetails.setCommitDate(cDate);
							cDetails.setUserId(userId);
							cDetails.setMessage(message);
							cDetails.setFileName(fileName.replace("null", ""));
							cDetails.setFileStatus(fileStatus.replace("null", ""));
							cDetails.setLinesAdded(linesAdded.replace("null", ""));
							cDetails.setLinesRemoved(linesRemoved.replace("null", ""));

							cDetailsRepository.save(cDetails);
						}
					}
				}
			} 
		} catch (Exception ex) {
			// TODO: handle exception
			errorMessage= ex.getMessage();
			ex.printStackTrace();
		}
		// Scheduler events to save in DB table
		if(!jsonResponse.isEmpty() && errorMessage.isEmpty() && errorMessage != null) {
			// has some commit details for particular time period and scheduler job status is success
			logger.info("commitDetailsSchedulerJobToSaveRecordsInDB() scheduler status is success");
			String status = "success";
			String errorMessage = "";
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			// capture and save scheduler status in gitservice_scheduler_status table in DB for successful scheduler job
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, status, errorMessage, serviceExecTime);

		} if (jsonResponse.isEmpty()) {
			// sometimes may not have commit details records for particular time period
			// consider this scenario is success but there is no records
			logger.info("commitDetailsSchedulerJobToSaveRecordsInDB() may not have commit details records for particular time period "+startDate+" + and + "+endDate+"");
			String status = "success";
			String errorMessage = "Sceduler completed Job but there is no commit details records between "+startDate+" + and + "+endDate+"";
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			// capture and save scheduler status in gitservice_scheduler_status table for there is no commit details
			// record for particular time period
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, status, errorMessage, serviceExecTime);

		} if (errorMessage != null) {
			// has some exception while running scheduler 
			logger.info("commitDetailsSchedulerJobToSaveRecordsInDB() scheduler status failure");
			String status = "failure";
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			// log exception details in gitservice_scheduler_status table in DB
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, status, errorMessage, serviceExecTime);
		}
	}
}
