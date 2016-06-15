package com.hpe.sm.train;

import java.util.ArrayList;
import java.util.List;

public class Change implements Comparable<Change> {
	private String ID;
	private List<String> features;
	private boolean result;
	private String description;
	private int amount;
	

	public Change(){
		ID = new String();
		features = new ArrayList<String>();
		result = false;
		description = "";
		amount = 0;
	}
	
	public Change(String ID, List<String> features, boolean result, String description, int amount){
		this.ID = ID;
		this.features = features;
		this.result = result;
		this.description = description;
		this.amount = amount;
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
	
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	//@Override
	public int compareTo(Change o) {
		return this.ID.compareTo(o.getID());
	}
	
}
