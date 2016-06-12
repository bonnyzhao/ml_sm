package com.hpe.sm;

import java.util.Arrays;
import org.apache.spark.sql.functions.*;
import org.apache.spark.sql.api.java.UDF1;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.param.Params;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.*;

public class BinaryClassifierFromText {

	public static DataFrame loadFromCsv(SQLContext sc, String filePath, String featureColumn, String labelColumn) {
		DataFrame df = sc.read().format("com.databricks.spark.csv").option("header", "true") // Use
																			// first
																			// line
																			// of
																			// all
																			// files
																			// as
																			// header
				.option("inferSchema", "true") // Automatically infer data types
				.option("nullValue", "null").option("treatEmptyValuesAsNulls", "true").option("delimiter", ",")
				.option("quote", "\u0000")
				// .schema(customSchema)
				.load(filePath);
		df.registerTempTable("data");

		
		UDF1<String, Double> label_converter = new UDF1<String, Double>() {
			  public Double call(final String str) throws Exception {
				  try{
					  return Double.valueOf(str) > 0 ? 1.0:0.0;
				  }catch(Exception e){
					  System.out.println("error here:"+e.getMessage());
					  return 0.0;
				  }
			  }
		};
		sc.udf().register("label_converter", label_converter, DataTypes.DoubleType);		
		DataFrame df2 = sc.sql("SELECT " + featureColumn + " as text, label_converter("  + labelColumn + ") AS label FROM data");

		//df2 = df2.withColumn("label", label_converter.call(df2.col("label")));
		//df2.select(label_converter.call(t1)("label_converter", df2.col("label")));
		 /*StructType customSchema = new StructType(new StructField[] { new
		 StructField("label", DataTypes.DoubleType, true, Metadata.empty()),
		 });*/
		 
		df2.printSchema();
		df2.show(10);
		return df2;
	}
 
		
	public static DataFrame[] split_data(DataFrame data, double ratio) {
		DataFrame[] splits = data.randomSplit(new double[] { ratio, 1.0 - ratio }, 12345);
		return splits;
	}

	// TODO: to validate usage
	public static DataFrame loadFromSQL(SQLContext sc, String tablename) {
		Map<String, String> options = new HashMap<String, String>();
		options.put("url", "jdbc:postgresql:dbserver");
		// options.put("dbtable", "schema.tablename");
		options.put("dbtable", tablename);

		DataFrame jdbcDF = sc.read().format("jdbc").options(options).load();
		return jdbcDF;
	}

    
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		SparkConf conf = new SparkConf().setAppName("Linear Regression Example").setMaster("local[2]");
//		JavaSparkContext sc = new JavaSparkContext(conf);
//		SQLContext sqlContext = new SQLContext(sc);
//		// INCIDENT_ID OWNER_NAME AFFECTED_ITEM LOGICAL_NAME TITLE SUBCATEGORY
//		// PRODUCT_TYPE DEPT HP_ISSUE_TYPE_ID HP_PROD_SPEC_ID ASSIGNMENT BREACH
//
//		// Register the DataFrame as a table.
//		DataFrame df = loadFromCsv(sqlContext, "C:/share/coursera/SM_Project/clean_change_incident.csv",  "DESCRIPTION", "count_day_90");					
//		
//		DataFrame[] splits = split_data(df, 0.8);
//		DataFrame training = splits[0];
//		DataFrame test = splits[1];
//		
//	    RegexTokenizer regexTokenizer = new RegexTokenizer()
//	    	      .setInputCol("text")
//	    	      .setOutputCol("words")
//	    	      .setPattern("\\W");  // alternatively .setPattern("\\w+").setGaps(false);
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
//		  .setRegParam(0.01)
//		  .setLabelCol("label")
//		  .setFeaturesCol("features")
//		  .setProbabilityCol("probability");
//		  //.setElasticNetParam(0.8);
//		
//		
//		
//		Pipeline pipeline = new Pipeline()
//				  .setStages(new PipelineStage[] {regexTokenizer,remover, hashingTF, idf, lr});
//
//		
//		PipelineModel model = pipeline.fit(training);
//		
//		// Make predictions on test documents.
//		DataFrame predictions = model.transform(test).select("prediction", "label", "probability");
//		BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator()
//				  .setLabelCol("label")
//				  .setRawPredictionCol("probability")	
//				  .setMetricName("areaUnderROC");
//		double roc = evaluator.evaluate(predictions);
//		System.out.println("roc = " + roc);
//		/*
//		predictions.registerTempTable("data2");
//		DataFrame pos = sqlContext.sql("SELECT prediction, label, probability FROM data2 where prediction>0");
//		for (Row r: pos.select("label", "prediction", "probability").take(300)) {
//		  System.out.println("(" + r.get(0) + ", " + r.get(1)  + ", " + r.get(2) + ")");
//		}
//		*/
//
//		
//	}

}
