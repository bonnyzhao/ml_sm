package com.hpe.sm.train;

import java.util.ArrayList;
import java.util.List;

public class Change implements Comparable<Change> {
	private String ID;
	private List<String> features;
	private boolean result;
	private String description;
	

	public Change(){
		ID = new String();
		features = new ArrayList<String>();
		result = false;
		description = "";
	}
	
	public Change(String ID, List<String> features, boolean result, String description){
		this.ID = ID;
		this.features = features;
		this.result = result;
		this.description = description;
	}
	
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}
	public List<String> getFeatures() {
		return features;
	}
	public void setFeatures(List<String> features) {
		this.features = features;
	}
	public boolean getResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	//@Override
	public int compareTo(Change o) {
		return this.ID.compareTo(o.getID());
	}
	
}
