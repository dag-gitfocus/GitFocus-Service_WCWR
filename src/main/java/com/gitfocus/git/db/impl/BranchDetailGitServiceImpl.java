package com.gitfocus.git.db.impl;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gitfocus.constants.GitFocusConstants;
import com.gitfocus.git.db.model.BranchDetails;
import com.gitfocus.git.db.model.BranchDetailsCompositeId;
import com.gitfocus.git.db.model.Units;
import com.gitfocus.git.db.service.IBranchDetailGitService;
import com.gitfocus.repository.BranchDetailsRepository;
import com.gitfocus.repository.UnitReposRepository;
import com.gitfocus.repository.UnitsRepository;
import com.gitfocus.util.GitFocusUtil;

/**
 * @author Tech Mahindra
 * Service class for BranchDetails and store values in branch_details table in DB 
 * 
 * NOTE : By default GitAPIJson gives max 30 records only for each RestAPI call but for some API have more than 30 records, hence we
 * to have to append page number and totalNoOfRecords/perPage for each URL's to fetch rest of the records
 */
@Service
public class BranchDetailGitServiceImpl implements IBranchDetailGitService {

	private static final Logger logger = LogManager.getLogger(BranchDetailGitServiceImpl.class.getSimpleName());

	public BranchDetailGitServiceImpl() {
		super();
		logger.info("BranchDetailServiceImpl init");
	}

	@Autowired
	private UnitsRepository uReposRepository;
	@Autowired
	private GitFocusConstants gitConstant;
	@Autowired
	private BranchDetailsRepository branchRepo;
	@Autowired
	private UnitReposRepository uRepository;
	@Autowired
	GitFocusUtil gitUtil;

	String unitOwner = null;
	List<String> reposName = null;
	int unitId = 0;
	String branchResult = null;
	String branchDetailURI = null;
	JSONArray jsonResponse = null;
	JSONObject branchObj = null;
	String branchName = null;
	int repoId = 0;
	String errorMessage = null;
	String serviceStatus = null;
	Date serviceExecTime = null;
	LocalDateTime localDateTime = LocalDateTime.now();
	Date branchCreatedTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	BranchDetails branchDetails = new BranchDetails();
	BranchDetailsCompositeId bCompositeId = new BranchDetailsCompositeId();

	/*
	 *  Method to get branch_details and store in branch_details table in DB 
	 */
	@Override
	public boolean save() {
		boolean result = false;
		List<Units> units = uReposRepository.findAll();
		if (units.isEmpty()) {
			return result;
		}
		units.forEach(response -> {
			unitId = response.getUnitId();
			unitOwner = response.getUnitOwner();
			reposName = uRepository.findReposName(unitId);

			reposName.forEach(repoName -> {
				for (int page = 1; page <= gitConstant.MAX_PAGE; page++) {
					branchDetailURI = gitConstant.BASE_URI + unitOwner + "/" + repoName + "/branches?" + "since="+ gitConstant.STARTDATE + "&"+ "until=" + gitConstant.ENDDATE +
							"page=" + page + "&per_page=" + gitConstant.TOTAL_RECORDS_PER_PAGE + "&";
					branchResult = gitUtil.getGitAPIJsonResponse(branchDetailURI);
					jsonResponse = new JSONArray(branchResult);
					try {
						for (int i = 0; i < jsonResponse.length(); i++) {
							branchObj = jsonResponse.getJSONObject(i);
							branchName = branchObj.getString("name");
							repoId = uRepository.findRepoId(repoName);

							bCompositeId.setUnitId(unitId);
							bCompositeId.setRepoId(repoId);
							bCompositeId.setBranchName(branchName);

							branchDetails.setbCompositeId(bCompositeId);
							// branchDetails.setParentBranch(parentBranch);
							branchRepo.save(branchDetails);

							logger.info("Records saved in branch_details table in DB");

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		});
		return true;
	}

	/**
	 * Method to execute scheduler jobs for branch details
	 * Get the branches
	 */
	@Override
	public void branchDetailsSchedulerJob() throws ParseException {
		// TODO Auto-generated method stub
		List<Units> units = uReposRepository.findAll();
		units.forEach(response -> {
			unitId = response.getUnitId();
			unitOwner = response.getUnitOwner();
			reposName = uRepository.findReposName(unitId);

			reposName.forEach(repoName -> {
				branchDetailsSchedulerJobToSaveRecordsInDB(repoName, unitId);
			});
		});
	}

	/**
	 * Method to save the values in branch_details table in DB through scheduler
	 * Capture scheduler events and log to gitservice_scheduler_status tables DB table
	 * @param repoName
	 */
	private void branchDetailsSchedulerJobToSaveRecordsInDB(String repoName, int unitId) {
		// TODO Auto-generated method stub
		logger.info("branchDetailsSchedulerJobToSaveRecordsInDB()" + repoName);
		String serviceName = "BranchDetail";
		List<String> existingBranchList = new ArrayList<String>();
		try {
			for (int page = 1; page <= gitConstant.SCHEDULER_MAX_PAGE; page++) {
				branchDetailURI = gitConstant.BASE_URI + unitOwner + "/" + repoName + "/branches?" + "page=" + page + "&per_page=" + gitConstant.SCHEDULER_TOTAL_RECORDS_PER_PAGE + "&";
				branchResult = gitUtil.getGitAPIJsonResponse(branchDetailURI);
				jsonResponse = new JSONArray(branchResult);
				for (int i = 0; i < jsonResponse.length(); i++) {
					branchObj = jsonResponse.getJSONObject(i);
					branchName = branchObj.getString("name");
					// get all existing branches from branch_details table
					existingBranchList = branchRepo.getAllExistingBranches();
					// if new branch has created on particular day, update the 
					// new branch name and created time in branch_details table through scheduler job
					if(!existingBranchList.stream().anyMatch(exBranchName -> exBranchName.equalsIgnoreCase(branchName))) {
						// update new branch name in branch_details table with created date
						repoId = uRepository.findRepoId(repoName);
						bCompositeId.setUnitId(unitId);
						bCompositeId.setRepoId(repoId);
						bCompositeId.setBranchName(branchName);
						branchDetails.setbCompositeId(bCompositeId);
						branchDetails.setCreatedTime(branchCreatedTime);
						branchRepo.save(branchDetails);

						// Scheduler events to save Scheduler events in gitservice_scheduler_status DB table
						// has some branch details for particular time period and scheduler job status is success
						logger.info("branchDetailsSchedulerJobToSaveRecordsInDB() scheduler status is success");
						serviceStatus = "success";
						serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
						String errorMessage = "";
						// capture and save scheduler status in gitservice_scheduler_status table in DB for successful scheduler job
						gitUtil.schedulerJobEventsToSaveInDB(repoName, branchName, serviceName, serviceStatus, errorMessage, serviceExecTime);
					}
				} 
			}
		}  catch (Exception e) {
			errorMessage = e.getMessage();
			e.printStackTrace();
		}
		// Scheduler events to save in DB table
		if (!jsonResponse.isEmpty()) {
			// sometimes may not have branch details records for particular time period
			// consider this scenario is success but there is no records
			logger.info("branchDetailsSchedulerJobToSaveRecordsInDB() may not have branch details records the date" + LocalDate.now());
			String serviceStatus = "success";
			String message = "Sceduler completed Job but there is no branch details records on " + LocalDate.now();
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			// capture and save scheduler status in gitservice_scheduler_status table for there is no branch details
			// record for particular time period
			gitUtil.schedulerJobEventsToSaveInDB(repoName, null, serviceName, serviceStatus, message, serviceExecTime);

		} if (errorMessage != null) {
			// has some exception while running scheduler 
			logger.info("branchDetailsSchedulerJobToSaveRecordsInDB() scheduler status failure");
			String serviceStatus = "failure";
			branchCreatedTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			serviceExecTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			// log exception details in gitservice_scheduler_status table in DB
			gitUtil.schedulerJobEventsToSaveInDB(repoName, null, serviceName, serviceStatus, errorMessage, serviceExecTime);
		}
	}
}
