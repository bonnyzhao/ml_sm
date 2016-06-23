package com.hpe.sm.restfulApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hpe.sm.train.Change;

/**
 * @author zhu-jing.wu@hpe.com
 * @date Jun 18, 2016 9:48:03 PM
 */
public class AttributesUnit {
	private String name;
	private String label;
	private String type;
	private String valueNum;
	private String value1;
	private String text1;
	private String value2;
	private String text2;
	private String value3;
	private String text3;
	private String value4;
	private String text4;
	private String value5;
	private String text5;
	
	
	public AttributesUnit(String name, String label, String type,
			String value1, String text1, String value2, String text2,
			String value3, String text3, String value4, String text4,
			String value5, String text5) {
		super();
		this.name = name;
		this.label = label;
		this.type = type;
		this.value1 = value1;
		this.text1 = text1;
		this.value2 = value2;
		this.text2 = text2;
		this.value3 = value3;
		this.text3 = text3;
		this.value4 = value4;
		this.text4 = text4;
		this.value5 = value5;
		this.text5 = text5;
	}
	
	public AttributesUnit(String name, String label, String type,
			String[] value, String[] text) {
		super();
		this.name = name;
		this.label = label;
		this.type = type;
		this.value1 = value[0];
		this.text1 = text[0];
		this.value2 = value[1];
		this.text2 = text[1];
		this.value3 = value[2];
		this.text3 = text[2];
		this.value4 = value[3];
		this.text4 = text[3];
		this.value5 = value[4];
		this.text5 = text[4];
	}

	//for risk
	public AttributesUnit(String name, String label, String value,
			String type) {
		super();
		this.name = name;
		this.label = label;
		this.valueNum = value;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return valueNum;
	}
	public void setValue(String value) {
		this.valueNum = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getText1() {
		return text1;
	}

	public void setText1(String text1) {
		this.text1 = text1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}

	public String getText2() {
		return text2;
	}

	public void setText2(String text2) {
		this.text2 = text2;
	}

	public String getValue3() {
		return value3;
	}

	public void setValue3(String value3) {
		this.value3 = value3;
	}

	public String getText3() {
		return text3;
	}

	public void setText3(String text3) {
		this.text3 = text3;
	}

	public String getValue4() {
		return value4;
	}

	public void setValue4(String value4) {
		this.value4 = value4;
	}

	public String getText4() {
		return text4;
	}

	public void setText4(String text4) {
		this.text4 = text4;
	}

	public String getValue5() {
		return value5;
	}

	public void setValue5(String value5) {
		this.value5 = value5;
	}

	public String getText5() {
		return text5;
	}

	public void setText5(String text5) {
		this.text5 = text5;
	}
	
	public static Change attributes2Change(AttributesUnit[] attributes, String desc){
		Change change = new Change();
		change.setDescription(desc);
		
		List<String> features = new ArrayList<String>();
		for(AttributesUnit att : attributes){
			if(att != null){
				switch (att.getName()){
				case "assignment.group" : {
					features.add("AssignGroup " + att.getText1());
					break;
				} case "cordinator" : {
					features.add("Coordinator " + att.getText1());
					break;
				} case "affected.item" : {
					features.add("Logical " + att.getText1());
					break;
				} case "model" : {
					features.add("Gl " + att.getText1());
					break;
				} case "category" : {
					features.add("Category " + att.getText1());
					break;
				}
				}
			}
		}
		change.setFeatures(features);
		return change;
	}
}

