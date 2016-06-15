package com.hpe.sm.train;

import java.util.ArrayList;
import java.util.List;

public class ChangeImpactResult {
	int HttpCode = 0;
	String HttpMsg = "";
	double possibility = 0;
	Change[] relatedChanges = new Change[5];
	
	
	public ChangeImpactResult(int httpCode, String httpMsg, double possibility,
			Change[] relatedChanges) {
		HttpCode = httpCode;
		HttpMsg = httpMsg;
		this.possibility = possibility;
		this.relatedChanges = relatedChanges;
	}
	
	public ChangeImpactResult(){
		
	}
	
	public double getPossibility() {
		return possibility;
	}
	public void setPossibility(double possibility) {
		this.possibility = possibility;
	}
	public Change[] getRelatedChanges() {
		return relatedChanges;
	}
	public void setRelatedChanges(Change[] relatedChanges) {
		this.relatedChanges = relatedChanges;
	}

	public int getHttpCode() {
		return HttpCode;
	}

	public void setHttpCode(int httpCode) {
		HttpCode = httpCode;
	}

	public String getHttpMsg() {
		return HttpMsg;
	}

	public void setHttpMsg(String httpMsg) {
		HttpMsg = httpMsg;
	}
	
}
