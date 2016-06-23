package com.hpe.sm.sla;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;

import modules.Incident;
import scala.util.parsing.json.JSONObject;



public class textPredictor {
	private static Logger logger;
	public static PipelineModel loadModel(String path){
		return PipelineModel.load(path);
	}
	
	public static DataFrame createDataFrame(String title, String description, String incident_id, JavaSparkContext jsc,SQLContext sqlContext ){		
		Incident in = new Incident();
		in.setTitle(title);
		in.setDescription(description);
		return in.toDataFrame(jsc, sqlContext);
	}
	
	public static DataFrame predictOne(String title, String description, String incident_id, JavaSparkContext jsc,SQLContext sqlContext, PipelineModel model ){
		DataFrame data = createDataFrame(title, description, incident_id, jsc, sqlContext);
		data = model.transform(data);
		return data;
	}
	
	public static DataFrame transformtAll(DataFrame df,  PipelineModel model,  String col, String... cols){
		df = model.transform(df);
		return df.select(col, cols);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		logger = Logger.getLogger(textPredictor.class);
		//BasicConfigurator.configure();

		
		SparkConf conf = new SparkConf().setAppName("Linear Regression Example").setMaster("local[2]");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(jsc);
		
		String affected_service = "e-mail exchange infrastructure";
		String model_path = "ml_models/textPredictor_"+affected_service;
		String data_path = "ml_data/"+affected_service+".parquet";
		String transformed_data_path = "ml_data/"+affected_service+"_tansformed.csv";
		logger.info("loading model : " + model_path);
		PipelineModel model = loadModel(model_path);
		/*
		String title = "";
		String description = "";
		try {
			description = new Scanner(new File("tmp.txt")).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(description);
		
		DataFrame df = predictOne(title,  description,  "1",  jsc, sqlContext, model );
		*/
		String[] textColumns = new String[] { "TITLE", "DESCRIPTION" };
		String[] otherColumns = new String[] { "INCIDENT_ID" };
		DataFrame df = textPredictorBuilder.loadFromParquet(sqlContext, data_path, "SLA_BREACH", textColumns,
				otherColumns);
		df = df.cache();
		String test_metrix = binaryTest.testModelMetrics(model, df, true, sqlContext, textPredictorBuilder.labelCol, textPredictorBuilder.predictionCol,
				textPredictorBuilder.probabilityCol);
		System.out.println("testing metrix:");
		System.out.println(test_metrix);

		DataFrame transformed = transformtAll(df,  model, textPredictorBuilder.labelCol, textPredictorBuilder.probabilityCol, textPredictorBuilder.predictionCol);
		transformed.write().format("csv").save(transformed_data_path);
		
	}
	

}
