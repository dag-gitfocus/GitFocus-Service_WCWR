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
import com.gitfocus.git.db.model.PullCommit;
import com.gitfocus.git.db.model.PullCommitCompositeId;
import com.gitfocus.git.db.model.Units;
import com.gitfocus.git.db.service.IPullCommitGitService;
import com.gitfocus.repository.BranchDetailsRepository;
import com.gitfocus.repository.CommitDetailsRepository;
import com.gitfocus.repository.GitFocusSchedulerRepository;
import com.gitfocus.repository.PullCommitRepository;
import com.gitfocus.repository.PullMasterRepository;
import com.gitfocus.repository.UnitReposRepository;
import com.gitfocus.repository.UnitsRepository;
import com.gitfocus.util.GitFocusUtil;

/**
 * @author Tech Mahindra Service class for PullCommit and store values in pull_commit table in DB
 * 
 * NOTE : By default GitAPIJson gives max 30 records only for each RestAPI call but for some API have more than 30 records, hence we to
 * append page number and totalNoOfRecords/perPage for each URL's to fetch rest of the records
 * 
 */
@Service
public class PullCommitGitServiceImpl implements IPullCommitGitService {

	private static final Logger logger = LogManager.getLogger(PullCommitGitServiceImpl.class.getSimpleName());

	public PullCommitGitServiceImpl() {
		super();
		logger.info("PullCommitServiceImpl init");
	}

	@Autowired
	private UnitReposRepository uRepository;
	@Autowired
	private UnitsRepository uReposRepository;
	@Autowired
	private GitFocusConstants gitConstant;
	@Autowired
	GitFocusUtil gitUtil;
	@Autowired
	private CommitDetailsRepository commitRepo;
	@Autowired
	private PullCommitRepository pullCommitRepo;
	@Autowired
	private PullMasterRepository pullMasterRepo;
	@Autowired 
	GitFocusSchedulerRepository gitFocusSchedulerRepo;
	@Autowired
	private BranchDetailsRepository branchRepo;

	int repoId = 0;
	int unitId = 0;
	String unitOwner = null;
	List<String> reposName = null;
	String pullCommitResult = null;
	String pullCommitURI = null;
	JSONArray jsonResponse = null;
	JSONObject jsonObj = null;
	String branchName = null;
	String sha_id = null;
	List<String> branches = null;
	List<String> pullNoList = null;
	List<Units> units = null;
	int pullNum = 0;
	String errorMessage = null;
	LocalDateTime localDateTime = LocalDateTime.now();
	Date serviceExecTime = null;
	PullCommit pCommit = new PullCommit();
	PullCommitCompositeId pullCompositeId = new PullCommitCompositeId();

	@Override
	public boolean save() throws ParseException {
		// TODO Auto-generated method stub
		boolean result = false;
		units = uReposRepository.findAll();
		if (units.isEmpty()) {
			return result;
		} else if (!units.isEmpty()) {

			units.forEach(response -> {
				unitId = response.getUnitId();
				unitOwner = response.getUnitOwner();
				reposName = uRepository.findReposName(unitId);

				reposName.forEach(repoName -> {
					repoId = uRepository.findRepoId(repoName);

					// get pull numbers for each pull request
					pullNoList = pullMasterRepo.findPullNo(repoId);
					pullNoList.forEach(pullNo -> {
						for (int page = 1; page <= gitConstant.MAX_PAGE; page++) {
							pullCommitURI = gitConstant.BASE_URI + unitOwner + "/" + repoName
									+ "/pulls/"+pullNo+"/commits?"+"state=all"+"&" + "since="+ gitConstant.STARTDATE + "&"+ "until=" + gitConstant.ENDDATE + "page=" + page  + "&per_page=" + gitConstant.TOTAL_RECORDS_PER_PAGE+ "&";
							pullNum = Integer.parseInt(pullNo);

							// sometimes pull number might be 0
							// in that case just ignore the pull request which has pull number 0
							if(pullNum == 0) {
								continue;
							} else {
								pullCommitResult = gitUtil.getGitAPIJsonResponse(pullCommitURI);
								jsonResponse = new JSONArray(pullCommitResult);

								try {
									jsonResponse = new JSONArray(pullCommitResult);
									for (int i = 0; i < jsonResponse.length(); i++) {
										jsonObj = jsonResponse.getJSONObject(i);
										sha_id = jsonObj.getString("sha");
										// get branches based on repoId and sha_id
										// sometimes one branch has multiple repoId and sha_id
										branches = commitRepo.getBranchNameByShaIdAndRepoId(repoId, sha_id);
										branches.forEach(branchName -> {
											repoId = uRepository.findRepoId(repoName);
											pullCompositeId.setRepoId(repoId);
											pullCompositeId.setPullNumber(Integer.parseInt(pullNo));
											pullCompositeId.setCommitId(sha_id);
											pullCompositeId.setBranchName(branchName);

											pCommit.setpCompositeId(pullCompositeId);
											pCommit.setUnitId(unitId);

											pullCommitRepo.save(pCommit);

											logger.info("Records saved in pull_commit table in DB");
										});
									}
								}
								catch (JSONException e) {
									// TODO: handle exception
									e.printStackTrace();
								}
							}
						}
					});
				});
			});
		}
		return true;
	}

	/**
	 * Method to execute scheduler jobs for pull commit details
	 */
	@Override
	public void pullCommitSchedulerJob() {
		// TODO Auto-generated method stub

		List<Units> units = (List<Units>) uReposRepository.findAll();
		units.forEach(response -> {
			unitId = response.getUnitId();
			unitOwner = response.getUnitOwner();
			reposName = uRepository.findReposName(unitId);

			reposName.forEach(repoName -> {
				repoId = uRepository.findRepoId(repoName);

				// get branches for repository
				branches = branchRepo.getBranchList(repoId);

				branches.forEach(branchName -> {
					pullCommitSchedulerJobToSaveRecordsInDB(repoName, branchName, unitId, repoId);
				});
			});
		});
	}

	/**
	 * 
	 * @param repoName
	 * @param unitId
	 * @param repoId
	 */
	private void pullCommitSchedulerJobToSaveRecordsInDB(String repoName, String branchName, int unitId, int repoId) {
		// TODO Auto-generated method stub
		logger.info("pullCommitSchedulerJobToSaveRecordsInDB()" + repoName + repoId);
		String serviceName = "PullCommit";
		String status;
		int pullNumber;

		//get the last scheduler status for each repository and branch whether its success or failure
		status = gitFocusSchedulerRepo.getSeriveStatusForPullCommit(repoName, serviceName);

		// getting records first time from table gitservice_scheduler_status might be null in status column
		// if service status success then get last pull number 
		if(status == null || status.equalsIgnoreCase("success")) {
			pullNumber = pullCommitRepo.getlastSuccessfulPullNumber(repoId);
			pullNoList = pullMasterRepo.findPullNoAfterLastSucceessfulRun(pullNumber);
		}
		// if service status failure then get last failure pull_number
		else if (status.equalsIgnoreCase("failure")) {
			// get the last failure pull_number
			pullNumber = pullCommitRepo.getlastFailurePullNumber(repoId);
			pullNoList = pullMasterRepo.findPullNoFromLastFailureRun(pullNumber);
		}
		try {
			pullNoList.forEach(pullNo -> {
				for (int page = 1; page <= gitConstant.SCHEDULER_MAX_PAGE; page++) {
					pullCommitURI = gitConstant.BASE_URI + unitOwner + "/" + repoName
							+ "/pulls/"+pullNo+"/commits?"+"state=all"+"&" + "page=" + page  + "&per_page=" + gitConstant.SCHEDULER_TOTAL_RECORDS_PER_PAGE+ "&";

					pullNum = Integer.parseInt(pullNo);

					pullCommitResult = gitUtil.getGitAPIJsonResponse(pullCommitURI);
					jsonResponse = new JSONArray(pullCommitResult);

					jsonResponse = new JSONArray(pullCommitResult);
					for (int i = 0; i < jsonResponse.length(); i++) {
						jsonObj = jsonResponse.getJSONObject(i);
						sha_id = jsonObj.getString("sha");
						// get branches based on repoId and sha_id
						// sometimes one branch has multiple repoId and sha_id
						branches = commitRepo.getBranchNameByShaIdAndRepoId(repoId, sha_id);
						branches.forEach(bName -> {
							pullCompositeId.setRepoId(repoId);
							pullCompositeId.setPullNumber(Integer.parseInt(pullNo));
							pullCompositeId.setCommitId(sha_id);
							pullCompositeId.setBranchName(bName);

							pCommit.setpCompositeId(pullCompositeId);
							pCommit.setUnitId(unitId);

							pullCommitRepo.save(pCommit);
						});
					}
				}
			});
		}catch (JSONException ex) {
			// TODO: handle exception
			errorMessage = ex.getMessage();
			ex.printStackTrace();
		}
		// Scheduler events to save in DB table
		if (pullNoList != null && !pullNoList.isEmpty() && errorMessage == null) {
			// has some pull commit details and scheduler job status is success
			logger.info("pullCommitSchedulerJobToSaveRecordsInDB() scheduler status is success");
			String serviceStatus = "success";
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			String errorMessage = "";
			// capture and save scheduler status in gitservice_scheduler_status table in DB for successful scheduler job
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);
		} if (pullNoList == null || pullNoList.isEmpty()) {
			// sometimes may not have pull commit details records for particular time period
			// consider this scenario is success but there is no records
			logger.info("pullCommitSchedulerJobToSaveRecordsInDB() may not have pull commit details on " + LocalDate.now());
			String serviceStatus = "success";
			String errorMessage = "Sceduler completed Job but there is no PULL commit details records on" + LocalDate.now();
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			// capture and save scheduler status in gitservice_scheduler_status table for there is no commit details
			// record for particular time period
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);

		}
		if (errorMessage != null) {
			// has some exception while running scheduler 
			logger.info("pullCommitSchedulerJobToSaveRecordsInDB() scheduler status failure");
			String serviceStatus = "failure";
			serviceExecTime = Date.from(localDateTime.atZone( ZoneId.systemDefault()).toInstant());
			// log exception details in gitservice_scheduler_status table in DB
			gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);
		}
	}
}