package com.gitfocus.repository;

import java.sql.Timestamp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gitfocus.git.db.model.GitServiceSchedulersStatus;

/**
 * @author Tech Mahindra 
 * Repository class for GitServiceSchedulersStatus table in DB
 */

@Repository
public interface GitFocusSchedulerRepository extends JpaRepository<GitServiceSchedulersStatus, String> {

	/**
	 * 
	 * @param repoName
	 * @param branchName
	 * @param serviceName
	 * @return status
	 */
	@Query(value = "SELECT status FROM wcwr_dev.gitservice_scheduler_status where repository_name=:repoName and branch_name=:branchName\r\n" + 
			"and service_name =:serviceName order by repository_name, branch_name DESC LIMIT 1", nativeQuery = true)
	String getSeriveStatus(String repoName, String branchName, String serviceName);

	/**
	 * 
	 * @param repoName
	 * @param branchName
	 * @param serviceName
	 * @return service_exec_time
	 */
	@Query(value = "SELECT service_exec_time FROM wcwr_dev.gitservice_scheduler_status where repository_name=:repoName and branch_name=:branchName\\r\\n\" + \r\n" + 
			"			\"and service_name =:serviceName order by repository_name, branch_name DESC LIMIT 1", nativeQuery = true)
	Timestamp getLastExecTime(String repoName, String branchName, String serviceName);

	/**
	 * 
	 * @param repoName
	 * @param serviceName
	 * @return
	 */
	@Query(value = "SELECT status FROM wcwr_dev.gitservice_scheduler_status where repository_name=:repoName and service_name =:serviceName order by repository_name DESC LIMIT 1", nativeQuery = true)
	String getSeriveStatusForPullCommit(String repoName, String serviceName);
} 

