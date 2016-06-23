package com.hpe.sm.restfulApi;

/**
 * @author zhu-jing.wu@hpe.com
 * @date Jun 19, 2016 10:18:14 PM
 */
public class FeaturesUnit {
	String id;
	String title;
	int amount;
	
	public FeaturesUnit(String id, String title, int amount) {
		super();
		this.id = id;
		this.title = title;
		this.amount = amount;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
}

