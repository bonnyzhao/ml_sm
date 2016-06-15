package com.hpe.sm.train;

public class InputedFeatures {
	String id = null;
	String category = null;
	String assignGroup = null;
	String coordinator = null;
	String risk = null;
	String priority = null;
	String gl = null;
	String logicalName = null;
	String service = null;
	String description = null;
	public InputedFeatures(String id, String category, String assignGroup,
			String coordinator, String risk, String priority, String gl,
			String logicalName, String service, String description) {
		super();
		this.id = id;
		this.category = category;
		this.assignGroup = assignGroup;
		this.coordinator = coordinator;
		this.risk = risk;
		this.priority = priority;
		this.gl = gl;
		this.logicalName = logicalName;
		this.service = service;
		this.description = description;
	}
	
	public InputedFeatures(){
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAssignGroup() {
		return assignGroup;
	}

	public void setAssignGroup(String assignGroup) {
		this.assignGroup = assignGroup;
	}

	public String getCoordinator() {
		return coordinator;
	}

	public void setCoordinator(String coordinator) {
		this.coordinator = coordinator;
	}

	public String getRisk() {
		return risk;
	}

	public void setRisk(String risk) {
		this.risk = risk;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getGl() {
		return gl;
	}

	public void setGl(String gl) {
		this.gl = gl;
	}

	public String getLogicalName() {
		return logicalName;
	}

	public void setLogicalName(String logicalName) {
		this.logicalName = logicalName;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString(){
		return id + "\t" + category + "\t" + assignGroup + "\t" + coordinator + "\t" + risk
				+ "\t" + priority + "\t" + gl + "\t" + logicalName + "\t" + service + "\t" + description;
		
	}
	
}
