package com.hpe.sm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

//C:\share\coursera\knowledege shareing\SLA_Predict\hpit_incident_clean1.csv
public class logistic_regression_sample {

	public static class Incident implements Serializable {
		public String getINCIDENT_ID() {
			return INCIDENT_ID;
		}

		public void setINCIDENT_ID(String iNCIDENT_ID) {
			INCIDENT_ID = iNCIDENT_ID;
		}

		public String getOWNER_NAME() {
			return OWNER_NAME;
		}

		public void setOWNER_NAME(String oWNER_NAME) {
			OWNER_NAME = oWNER_NAME;
		}


		public String getAFFECTED_ITEM() {
			return AFFECTED_ITEM;
		}

		public void setAFFECTED_ITEM(String aFFECTED_ITEM) {
			AFFECTED_ITEM = aFFECTED_ITEM;
		}

		public String getLOGICAL_NAME() {
			return LOGICAL_NAME;
		}

		public void setLOGICAL_NAME(String lOGICAL_NAME) {
			LOGICAL_NAME = lOGICAL_NAME;
		}

		public String getTITLE() {
			return TITLE;
		}

		public void setTITLE(String tITLE) {
			TITLE = tITLE;
		}

		public String getSUBCATEGORY() {
			return SUBCATEGORY;
		}

		public void setSUBCATEGORY(String sUBCATEGORY) {
			SUBCATEGORY = sUBCATEGORY;
		}

		public String getPRODUCT_TYPE() {
			return PRODUCT_TYPE;
		}

		public void setPRODUCT_TYPE(String pRODUCT_TYPE) {
			PRODUCT_TYPE = pRODUCT_TYPE;
		}

		public String getDEPT() {
			return DEPT;
		}

		public void setDEPT(String dEPT) {
			DEPT = dEPT;
		}

		public String getHP_ISSUE_TYPE_ID() {
			return HP_ISSUE_TYPE_ID;
		}

		public void setHP_ISSUE_TYPE_ID(String hP_ISSUE_TYPE_ID) {
			HP_ISSUE_TYPE_ID = hP_ISSUE_TYPE_ID;
		}

		public String getHP_PROD_SPEC_ID() {
			return HP_PROD_SPEC_ID;
		}

		public void setHP_PROD_SPEC_ID(String hP_PROD_SPEC_ID) {
			HP_PROD_SPEC_ID = hP_PROD_SPEC_ID;
		}

		public String getASSIGNMENT() {
			return ASSIGNMENT;
		}

		public void setASSIGNMENT(String aSSIGNMENT) {
			ASSIGNMENT = aSSIGNMENT;
		}
		public double getLabel() {
			return label;
		}

		public void setLabel(double label) {
			this.label = label;
		}
		
		private String INCIDENT_ID;
		private String OWNER_NAME;
		private double label;
		private String AFFECTED_ITEM;
		private String LOGICAL_NAME;
		private String TITLE;
		private String SUBCATEGORY;
		private String PRODUCT_TYPE;
		private String DEPT;
		private String HP_ISSUE_TYPE_ID;
		private String HP_PROD_SPEC_ID;
		private String ASSIGNMENT;

	}
	

//	public static void main(String[] args) {
//		SparkConf conf = new SparkConf().setAppName("Linear Regression Example").setMaster("local");
//		JavaSparkContext sc = new JavaSparkContext(conf);
//		SQLContext sqlContext = new SQLContext(sc);
//		// INCIDENT_ID OWNER_NAME AFFECTED_ITEM LOGICAL_NAME TITLE SUBCATEGORY
//		// PRODUCT_TYPE DEPT HP_ISSUE_TYPE_ID HP_PROD_SPEC_ID ASSIGNMENT BREACH
//		JavaRDD<Incident> incidents_rdd = sc
//				.textFile("C:\\share\\coursera\\knowledege shareing\\SLA_Predict\\hpit_incident_clean1.txt")
//				.map(new Function<String, Incident>() {
//					public Incident call(String line) throws Exception {
//						String[] parts = line.split("\\t");
//						Incident incident = new Incident();
//						incident.setINCIDENT_ID(parts[0]);
//						incident.setOWNER_NAME(parts[1]);
//						incident.setAFFECTED_ITEM(parts[2]);
//						incident.setLOGICAL_NAME(parts[3]);
//						incident.setTITLE(parts[4]);
//						incident.setSUBCATEGORY(parts[5]);
//						incident.setPRODUCT_TYPE(parts[6]);
//						incident.setDEPT(parts[7]);
//						incident.setHP_ISSUE_TYPE_ID(parts[8]);
//						incident.setHP_PROD_SPEC_ID(parts[9]);
//						incident.setASSIGNMENT(parts[10]);
//						System.out.println(parts[11]);
//						incident.setLabel(Double.parseDouble(parts[11]));
//						return incident;
//					}
//				});
//		
//		DataFrame data = sqlContext.createDataFrame(incidents_rdd, Incident.class);
//		
//		//return new LabeledPoint(Double.parseDouble(parts[0]), Vectors.dense(v));
//		data.registerTempTable("incidents");
//		
//		DataFrame[] splits = data.randomSplit(new double[] {0.9, 0.1}, 12345);
//		DataFrame training = splits[0];
//		DataFrame test = splits[1];
//		
//		//DataFrame teenagers = sqlContext.sql("SELECT name FROM incidents WHERE age >= 13 AND age <= 19");
//	    RegexTokenizer regexTokenizer = new RegexTokenizer()
//	    	      .setInputCol("TITLE")
//	    	      .setOutputCol("words")
//	    	      .setPattern("\\W");  // alternatively .setPattern("\\w+").setGaps(false);
//		/*Tokenizer tokenizer = new Tokenizer()
//				  .setInputCol("TITLE")
//				  .setOutputCol("words");*/
//	    
//	    StopWordsRemover remover = new StopWordsRemover()
//	    		  .setInputCol(regexTokenizer.getOutputCol())
//	    		  .setOutputCol("filtered_words");
//	    
//		HashingTF hashingTF = new HashingTF()
//				  .setNumFeatures(1000)
//				  .setInputCol(remover.getOutputCol())
//				  .setOutputCol("rawFeatures");
//		
//		IDF idf = new IDF()
//				.setInputCol(hashingTF.getOutputCol())
//				.setOutputCol("features");
//				
//		LogisticRegression lr = new LogisticRegression();
//		System.out.println("LogisticRegression parameters:\n" + lr.explainParams() + "\n");
//		
//		lr.setMaxIter(10)
//		  .setRegParam(0.01);
//		
//		Pipeline pipeline = new Pipeline()
//				  .setStages(new PipelineStage[] {regexTokenizer,remover, hashingTF, idf, lr});
//
//		
//		PipelineModel model = pipeline.fit(training);
//		
//		// Make predictions on test documents.
//		DataFrame predictions = model.transform(test);
//		for (Row r: predictions.select("INCIDENT_ID", "TITLE", "filtered_words", "features", "probability", "prediction").take(3)) {
//		  System.out.println("(" + r.get(0) + ", " + r.get(1) + ", " + r.get(2) + ", " + r.get(3) + ") --> prob=" + r.get(4)
//		      + ", prediction=" + r.get(5));
//		}
//	}
}
