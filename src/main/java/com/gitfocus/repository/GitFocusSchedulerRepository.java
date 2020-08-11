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
	 * @return commitDate
	 */
	@Query(value = "SELECT status FROM wcwr_dev.gitservice_scheduler_status where repository_name=:repoName and branch_name=:branchName", nativeQuery = true)
	String getSeriveStatus(String repoName, String branchName);

	@Query(value = "SELECT service_exec_time FROM wcwr_dev.gitservice_scheduler_status where repository_name=:repoName and branch_name=:branchName", nativeQuery = true)
	Timestamp getLastExecTime(String repoName, String branchName);


} 

