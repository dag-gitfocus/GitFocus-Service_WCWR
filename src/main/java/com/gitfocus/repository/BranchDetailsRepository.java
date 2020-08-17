package com.gitfocus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gitfocus.git.db.model.BranchDetails;

/**
 * @author Tech Mahindra 
 * Repository interface for branch_details table in DB
 */
@Repository
public interface BranchDetailsRepository extends JpaRepository<BranchDetails, Object> {

	/**
	 * 
	 * @param repoId
	 * @return branchList
	 */
	@Query("select b.bCompositeId.branchName from BranchDetails b where b.bCompositeId.repoId=:repoId and b.bCompositeId.branchName!= 'staging' and b.bCompositeId.branchName!= 'master'")
	List<String> getBranchList(int repoId);

	/**
	 * 
	 * @return getAllExistingBranches
	 */
	@Query(value = "select branch_name from wcwr_dev.branch_details", nativeQuery = true)
	List<String> getAllExistingBranches();

}
