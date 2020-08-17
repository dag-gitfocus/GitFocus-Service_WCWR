package com.gitfocus.git.db.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Tech Mahindra 
 * Model class for branch_details table in DB
 */

@Entity
@Table(name = "branch_details", schema = "wcwr_dev")
public class BranchDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BranchDetails() {
		super();
	}

	@JsonIgnore
	@JoinColumns({ @JoinColumn(name = "unit_id", referencedColumnName = "unit_id"),
		@JoinColumn(name = "repo_id", referencedColumnName = "repo_id")})

	@EmbeddedId
	BranchDetailsCompositeId bCompositeId;

	@Column(name = "parent_branch")
	private String parentBranch;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time")
	private Date createdTime;

	/**
	 * 
	 * @return bCompositeId
	 */
	public BranchDetailsCompositeId getbCompositeId() {
		return bCompositeId;
	}

	/**
	 * 
	 * @param bCompositeId
	 */
	public void setbCompositeId(BranchDetailsCompositeId bCompositeId) {
		this.bCompositeId = bCompositeId;
	}

	/**
	 * 
	 * @return parentBranch
	 */
	public String getParentBranch() {
		return parentBranch;
	}

	/**
	 * 
	 * @param parentBranch
	 */
	public void setParentBranch(String parentBranch) {
		this.parentBranch = parentBranch;
	}

	/**
	 * 
	 * @return createdTime
	 */
	public Date getCreatedTime() {
		return createdTime;
	}

	/**
	 * 
	 * @param createdTime
	 */
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bCompositeId == null) ? 0 : bCompositeId.hashCode());
		result = prime * result + ((createdTime == null) ? 0 : createdTime.hashCode());
		result = prime * result + ((parentBranch == null) ? 0 : parentBranch.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BranchDetails other = (BranchDetails) obj;
		if (bCompositeId == null) {
			if (other.bCompositeId != null)
				return false;
		} else if (!bCompositeId.equals(other.bCompositeId))
			return false;
		if (createdTime == null) {
			if (other.createdTime != null)
				return false;
		} else if (!createdTime.equals(other.createdTime))
			return false;
		if (parentBranch == null) {
			if (other.parentBranch != null)
				return false;
		} else if (!parentBranch.equals(other.parentBranch))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BranchDetails [bCompositeId=" + bCompositeId + ", parentBranch=" + parentBranch + ", createdTime="
				+ createdTime + "]";
	}


}
