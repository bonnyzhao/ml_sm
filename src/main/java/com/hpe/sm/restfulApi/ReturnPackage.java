package com.hpe.sm.restfulApi;

/**
 * @author zhu-jing.wu@hpe.com
 * @date Jun 19, 2016 10:19:06 PM
 */
public class ReturnPackage {
	AttributesUnit[] attributes;
	FeaturesUnit[] features;
	public ReturnPackage(AttributesUnit[] attributes, FeaturesUnit[] features) {
		super();
		this.attributes = attributes;
		this.features = features;
	}
	public AttributesUnit[] getAttributes() {
		return attributes;
	}
	public void setAttributes(AttributesUnit[] attributes) {
		this.attributes = attributes;
	}
	public FeaturesUnit[] getFeatures() {
		return features;
	}
	public void setFeatures(FeaturesUnit[] features) {
		this.features = features;
	}
	
	
}

