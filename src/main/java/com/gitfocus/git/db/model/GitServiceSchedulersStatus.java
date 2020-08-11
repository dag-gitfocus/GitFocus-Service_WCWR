package com.gitfocus.git.db.model;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 
 * @author Tech Mahindra 
 * Scheduler class for CommitDetail/PullMaster/PullCommit/ReviewDetail Services
 */

@Entity
@Table(name = "gitservice_scheduler_status", schema = "wcwr_dev")
public class GitServiceSchedulersStatus {

	public GitServiceSchedulersStatus() {
		super();
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "repository_name")
	private String repositoryName;

	@Column(name = "branch_name")
	private String branchName;

	@Column(name = "service_name")
	private String serviceName;

	@Column(name = "error_exception")
	private String errorException;

	@Column(name = "status")
	private String status;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "service_exec_time")
	private Date serviceExecTime;

	/**
	 * 
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * 
	 * @return repositoryName
	 */
	public String getRepositoryName() {
		return repositoryName;
	}

	/**
	 * 
	 * @param repositoryName
	 */
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	/**
	 * 
	 * @return branchName
	 */
	public String getBranchName() {
		return branchName;
	}

	/**
	 * 
	 * @param branchName
	 */
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	/**
	 * 
	 * @return serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * 
	 * @param serviceName
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * 
	 * @return errorException
	 */
	public String getErrorException() {
		return errorException;
	}
	/**
	 * 
	 * @param errorException
	 */
	public void setErrorException(String errorException) {
		this.errorException = errorException;
	}

	/**
	 * 
	 * @return status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * 
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 
	 * @return serviceExecTime
	 */
	public Date getServiceExecTime() {
		return serviceExecTime;
	}

	/**
	 * 
	 * @param serviceExecTime
	 */
	public void setServiceExecTime(Date serviceExecTime) {
		this.serviceExecTime = serviceExecTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branchName == null) ? 0 : branchName.hashCode());
		result = prime * result + ((errorException == null) ? 0 : errorException.hashCode());
		result = prime * result + id;
		result = prime * result + ((repositoryName == null) ? 0 : repositoryName.hashCode());
		result = prime * result + ((serviceExecTime == null) ? 0 : serviceExecTime.hashCode());
		result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		GitServiceSchedulersStatus other = (GitServiceSchedulersStatus) obj;
		if (branchName == null) {
			if (other.branchName != null)
				return false;
		} else if (!branchName.equals(other.branchName))
			return false;
		if (errorException == null) {
			if (other.errorException != null)
				return false;
		} else if (!errorException.equals(other.errorException))
			return false;
		if (id != other.id)
			return false;
		if (repositoryName == null) {
			if (other.repositoryName != null)
				return false;
		} else if (!repositoryName.equals(other.repositoryName))
			return false;
		if (serviceExecTime == null) {
			if (other.serviceExecTime != null)
				return false;
		} else if (!serviceExecTime.equals(other.serviceExecTime))
			return false;
		if (serviceName == null) {
			if (other.serviceName != null)
				return false;
		} else if (!serviceName.equals(other.serviceName))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GitServiceSchedulersStatus [id=" + id + ", repositoryName=" + repositoryName + ", branchName="
				+ branchName + ", serviceName=" + serviceName + ", errorException=" + errorException + ", status="
				+ status + ", serviceExecTime=" + serviceExecTime + "]";
	}

}