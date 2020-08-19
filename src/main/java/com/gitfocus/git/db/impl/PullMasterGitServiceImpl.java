package com.gitfocus.git.db.impl;

import java.sql.Timestamp;
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
import com.gitfocus.git.db.model.PullMaster;
import com.gitfocus.git.db.model.PullMasterCompositeId;
import com.gitfocus.git.db.model.Units;
import com.gitfocus.git.db.service.IPullMasterGitService;
import com.gitfocus.repository.BranchDetailsRepository;
import com.gitfocus.repository.GitFocusSchedulerRepository;
import com.gitfocus.repository.PullMasterRepository;
import com.gitfocus.repository.TeamMembersRepository;
import com.gitfocus.repository.UnitReposRepository;
import com.gitfocus.repository.UnitsRepository;
import com.gitfocus.util.GitFocusUtil;

/**
 * @author Tech Mahindra 
 * Service class for PullMaster and store values in pull_master table in DB
 * 
 * NOTE : By default GitAPIJson gives max 30 records only for each RestAPI call but for some API have more than 30 records, hence we to
 * have to append page number and totalNoOfRecords/perPage for each URL's to fetch rest of the records
 * 
 */
@Service
public class PullMasterGitServiceImpl implements IPullMasterGitService {

	private static final Logger logger = LogManager.getLogger(PullMasterGitServiceImpl.class.getSimpleName());

	public PullMasterGitServiceImpl() {
		super();
		logger.info("PullMasterServiceImpl init");
	}

	@Autowired
	private UnitsRepository unitsRepository;
	@Autowired
	private GitFocusConstants gitConstant;
	@Autowired
	private UnitReposRepository uReposRepository;
	@Autowired
	private BranchDetailsRepository branchRepo;
	@Autowired
	GitFocusUtil gitUtil;
	@Autowired
	PullMasterRepository pMasterRepository;
	@Autowired
	private GitFocusConstants gitFocusConstant;
	@Autowired
	TeamMembersRepository teamMemRepos;
	@Autowired 
	GitFocusSchedulerRepository gitFocusSchedulerRepo;

	String pullResults = null;
	String pullMasterURI = null;
	String fromBranch = null;
	String toBranch = null;
	String userId = null;
	String unitOwner = null;
	List<String> reposName = null;
	String pullState = null;
	int commitCount = 0;
	boolean merged = false;
	String mergedBy = null;
	Object mergedByNull = null;
	Date closedAt = null;
	Date mergAt = null;
	JSONObject mergBy = null;
	String pullNoResults = null;
	int unitId = 0;
	int pullNo = 0;
	Date creTime = null;
	Date updTime = null;
	String pullNoUri = null;
	int pullId = 0;
	String user = null;
	int repoId = 0;
	Object cTime = null;
	Object mTime = null;
	String pullsResult = null;
	JSONObject pullNoObjJson = null;
	JSONObject pullObj = null;
	JSONObject pullNoObj = null;
	JSONObject pullObjHead = null;
	JSONObject pullObjBase = null;
	JSONObject pullObjUserId = null;
	String createdTime = null;
	String updatedTime = null;
	JSONArray jsonResponse = null;
	boolean result = false;
	List<String> branches = null;
	Timestamp startDate = null;
	LocalDateTime endDate = null;
	String errorMessage= null;
	LocalDateTime localDateTime = LocalDateTime.now();
	Date serviceExecTime = null;
	PullMasterCompositeId pullCompositeId = new PullMasterCompositeId();
	PullMaster pMaster = new PullMaster();

	/*
	 * Method to get all the pull request info and pull request based on pull number
	 */
	@Override
	public boolean save() {
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

				// get branches for repository
				branches = branchRepo.getBranchList(repoId);

				branches.forEach(branchName -> {
					for (int page = 1; page <= gitConstant.MAX_PAGE; page++) {
						// To get Pull review based on all the pull history
						pullMasterURI = gitFocusConstant.BASE_URI + unitOwner + "/" + repoName + "/pulls?" + "state=all"
								+ "&" + "since="+ gitConstant.STARTDATE + "&"+ "until=" + gitConstant.ENDDATE + "page=" + page + "&per_page=" + gitFocusConstant.TOTAL_RECORDS_PER_PAGE + "&";

						pullsResult = gitUtil.getGitAPIJsonResponse(pullMasterURI);
						jsonResponse = new JSONArray(pullsResult);

						for (int i = 0; i < jsonResponse.length(); i++) {

							pullObj = jsonResponse.getJSONObject(i);
							pullObjHead = pullObj.getJSONObject("head");
							pullObjBase = pullObj.getJSONObject("base");
							pullObjUserId = pullObj.getJSONObject("user");

							pullNo = pullObj.getInt("number");

							pullCompositeId.setRepoId(repoId);
							pullCompositeId.setPullNumber(pullNo);

							pMaster.setPullMasterCompositeId(pullCompositeId);

							pMaster.setUnitId(unitId);
							pullId = pullObj.getInt("id");
							fromBranch = pullObjHead.getString("ref");
							toBranch = pullObjBase.getString("ref");
							createdTime = pullObj.getString("created_at");
							updatedTime = pullObj.getString("updated_at");
							creTime = GitFocusUtil.stringToDate(createdTime);
							pullState = pullObj.getString("state");
							updTime = GitFocusUtil.stringToDate(updatedTime);
							userId = pullObjUserId.getString("login");

							// To get Pull review based on pull number -- START

							pullNoUri = gitFocusConstant.BASE_URI + unitOwner + "/" + repoName + "/pulls/" + pullNo
									+ "?" + "state=all" + "&" + "page=" + page + "&per_page="
									+ gitFocusConstant.TOTAL_RECORDS_PER_PAGE + "&";

							pullNoResults = gitUtil.getGitAPIJsonResponse(pullNoUri);
							pullNoObjJson = new JSONObject(pullNoResults);

							commitCount = pullNoObjJson.getInt("commits");
							merged = pullNoObjJson.getBoolean("merged");
							cTime = pullNoObjJson.get("closed_at");
							mTime = pullNoObjJson.get("merged_at");

							// merged_by
							Object mergValue = pullNoObjJson.get("merged_by");
							if (mergValue instanceof JSONObject) {
								mergBy = pullNoObjJson.getJSONObject("merged_by");
								mergedBy = mergBy.getString("login");
								pMaster.setMergedBy(mergedBy);
							} else {
								mergedByNull = pullNoObjJson.get("merged_by");
								pMaster.setMergedBy(String.valueOf(mergedByNull));
							}
							// closed_at
							if (!cTime.equals(null)) {
								closedAt = GitFocusUtil.stringToDate(String.valueOf(cTime));
								pMaster.setClosedAt(closedAt);
							} else {
								pMaster.setClosedAt(null);
							}
							// merged_at
							if (!mTime.equals(null)) {
								mergAt = GitFocusUtil.stringToDate(String.valueOf(mTime));
								pMaster.setMergedAt(mergAt);
							} else {
								pMaster.setMergedAt(null);
							}

							// To get Pull review based on pull number -- END

							// Store values in pull_master table in DB
							pMaster.setPullId(pullId);
							pMaster.setFromBranch(fromBranch);
							pMaster.setToBranch(toBranch); 
							pMaster.setCreatedTime(creTime);
							pMaster.setPullStatus(pullState);
							pMaster.setUserId(userId);
							pMaster.setUpdatedTime(updTime);
							pMaster.setCommitCount(commitCount);
							pMaster.setMerged(merged);
							pMaster.setClosedAt(closedAt);

							pMasterRepository.save(pMaster);

							logger.info("Records saved in PullMaster table in DB ");
						}
					}
				});
			});
		});
		return true;
	}

	/**
	 * Method to execute scheduler jobs for pull master
	 * Get the repository and branch
	 */
	@Override
	public void pullMasterSchedulerJob() {
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
					pullMasterSchedulerJobToSaveRecordsInDB(repoName, branchName);
				});
			});
		});
	}

	/**
	 * Method to save the values in pull_master table in DB through scheduler
	 * Capture scheduler events and log to gitservice_scheduler_status tables DB table
	 * @param repoName
	 * @param branchName
	 */
	private void pullMasterSchedulerJobToSaveRecordsInDB(String repoName, String branchName) {
		// TODO Auto-generated method stub
		logger.info("pullMasterSchedulerJobToSaveRecordsInDB()" + repoName + branchName);
		String serviceName = "PullMaster";
		String status;

		//get the last scheduler status for each repository whether its success or failure
		status = gitFocusSchedulerRepo.getSeriveStatus(repoName, branchName, serviceName);
		repoId = uReposRepository.findRepoId(repoName);

		// getting records first time from table gitservice_scheduler_status might be null in status column
		// if service status success then fetch last pull_number for each repository 
		if(status == null || status.equalsIgnoreCase("success")) {
			startDate = pMasterRepository.getLastSuccessfulPRCreatedTime(repoId, branchName);
			endDate = LocalDateTime.now();
		}
		// if service status failure then fetch last pull_number for failed repository
		else if (status.equalsIgnoreCase("failure")) {
			startDate = gitFocusSchedulerRepo.getLastExecTime(repoName, branchName, serviceName);
			endDate = LocalDateTime.now();
		}
		try {
			for (int page = 1; page <= gitConstant.SCHEDULER_MAX_PAGE; page++) {
				// To get Pull review based on all the pull history
				pullMasterURI = gitFocusConstant.BASE_URI + unitOwner + "/" + repoName + "/pulls?" + "state=all"
						+ "&" + "since="+ startDate + "&"+ "until=" + endDate + "page=" + page + "&per_page=" + gitFocusConstant.SCHEDULER_TOTAL_RECORDS_PER_PAGE + "&";

				pullsResult = gitUtil.getGitAPIJsonResponse(pullMasterURI);
				jsonResponse = new JSONArray(pullsResult);

				for (int i = 0; i < jsonResponse.length(); i++) {

					pullObj = jsonResponse.getJSONObject(i);
					pullObjHead = pullObj.getJSONObject("head");
					pullObjBase = pullObj.getJSONObject("base");
					pullObjUserId = pullObj.getJSONObject("user");

					pullNo = pullObj.getInt("number");

					pullCompositeId.setRepoId(repoId);
					pullCompositeId.setPullNumber(pullNo);

					pMaster.setPullMasterCompositeId(pullCompositeId);

					pMaster.setUnitId(unitId);
					pullId = pullObj.getInt("id");
					fromBranch = pullObjHead.getString("ref");
					toBranch = pullObjBase.getString("ref");
					createdTime = pullObj.getString("created_at");
					updatedTime = pullObj.getString("updated_at");
					creTime = GitFocusUtil.stringToDate(createdTime);
					pullState = pullObj.getString("state");
					updTime = GitFocusUtil.stringToDate(updatedTime);
					userId = pullObjUserId.getString("login");

					pullNoUri = gitFocusConstant.BASE_URI + unitOwner + "/" + repoName + "/pulls/" + pullNo
							+ "?" + "state=all" + "&" + "page=" + page + "&per_page="
							+ gitFocusConstant.SCHEDULER_TOTAL_RECORDS_PER_PAGE + "&";

					pullNoResults = gitUtil.getGitAPIJsonResponse(pullNoUri);
					pullNoObjJson = new JSONObject(pullNoResults);

					commitCount = pullNoObjJson.getInt("commits");
					merged = pullNoObjJson.getBoolean("merged");
					cTime = pullNoObjJson.get("closed_at");
					mTime = pullNoObjJson.get("merged_at");

					// merged_by
					Object mergValue = pullNoObjJson.get("merged_by");
					if (mergValue instanceof JSONObject) {
						mergBy = pullNoObjJson.getJSONObject("merged_by");
						mergedBy = mergBy.getString("login");
						pMaster.setMergedBy(mergedBy);
					} else {
						mergedByNull = pullNoObjJson.get("merged_by");
						pMaster.setMergedBy(String.valueOf(mergedByNull));
					}
					// closed_at
					if (!cTime.equals(null)) {
						closedAt = GitFocusUtil.stringToDate(String.valueOf(cTime));
						pMaster.setClosedAt(closedAt);
					} else {
						pMaster.setClosedAt(null);
					}
					// merged_at
					if (!mTime.equals(null)) {
						mergAt = GitFocusUtil.stringToDate(String.valueOf(mTime));
						pMaster.setMergedAt(mergAt);
					} else {
						pMaster.setMergedAt(null);
					}

					// Store values in pull_master table in DB
					pMaster.setPullId(pullId);
					pMaster.setFromBranch(fromBranch);
					pMaster.setToBranch(toBranch); 
					pMaster.setCreatedTime(creTime);
					pMaster.setPullStatus(pullState);
					pMaster.setUserId(userId);
					pMaster.setUpdatedTime(updTime);
					pMaster.setCommitCount(commitCount);
					pMaster.setMerged(merged);
					pMaster.setClosedAt(closedAt);

					pMasterRepository.save(pMaster);

				}
			} 
		} catch (Exception ex) {
			// TODO: handle exception
			errorMessage= ex.getMessage();
			ex.printStackTrace();
		}
		// Scheduler events to save in DB table
		if(!jsonResponse.isEmpty() && errorMessage.isEmpty() && errorMessage != null) {
			// has some PR details for particular time period and scheduler job status is success
			logger.info("pullMasterSchedulerJobToSaveRecordsInDB() scheduler status success");
			String serviceStatus = "success";
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			String errorMessage = "";
			// capture and save scheduler status in gitservice_scheduler_status table in DB for successful scheduler job
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);

		} if (jsonResponse.isEmpty()) {
			// sometimes may not have PR details records for particular time period
			// consider this scenario is success but there is no records
			logger.info("pullMasterSchedulerJobToSaveRecordsInDB() may not have PR details records for particular time period "+startDate+" + and + "+endDate+"");
			String serviceStatus = "success";
			String errorMessage = "Sceduler completed Job but there is no PR details records between "+startDate+" + and + "+endDate+"";
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			// capture and save scheduler status in gitservice_scheduler_status table for there is no PR 
			// record for particular time period
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);

		} if (errorMessage != null) {
			// has some exception while running scheduler 
			logger.info("pullMasterSchedulerJobToSaveRecordsInDB() scheduler status failure");
			String serviceStatus = "failure";
			serviceExecTime = Date.from(localDateTime.atZone( ZoneId.systemDefault()).toInstant());
			// log exception details in gitservice_scheduler_status table in DB
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);
		}
	}
}