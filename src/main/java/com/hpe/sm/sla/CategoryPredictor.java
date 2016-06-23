package com.hpe.sm.sla;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class CategoryPredictor {
	private static final String labelCol = "label";
	private static final String predictionCol = "prediction_text";
	private static final String probabilityCol = "probability_text";
	
	private static Logger theLogger = Logger.getLogger(CategoryPredictor.class.getName());
	public static DataFrame loadFromParquet(SQLContext sc, String filePath, String labelColumn, String[] categoryColumns,
			String[] otherColumns) {

		DataFrame df = sc.read().parquet(filePath);
		df.registerTempTable("data");

		UDF1<String, Double> label_converter = new UDF1<String, Double>() {
			public Double call(final String str) throws Exception {
				try {
					return str.equalsIgnoreCase("t") ? 1.0 : 0.0;
				} catch (Exception e) {
					System.out.println("error here:" + e.getMessage());
					return 0.0;
				}
			}
		};
		sc.udf().register("label_converter", label_converter, DataTypes.DoubleType);
		UDF1<String, String> category_converter = new UDF1<String, String>() {
			public String call(final String str) throws Exception {
				try {
					if (str == null || str.isEmpty()){
					return "N/A";}
					else{
						return str;
					}
				} catch (Exception e) {
					System.out.println("error here:" + e.getMessage());
					return "N/A";
				}
			}
		};
		sc.udf().register("category_converter", category_converter, DataTypes.StringType);
		ArrayList<String> tmp = new ArrayList<String>();
		for (String col:categoryColumns){
			tmp.add("category_converter("+col+") AS " + col);
		}
		DataFrame df2 = sc
				.sql("SELECT "+ StringUtils.join(tmp, ",") +",  label_converter("
						+ labelColumn + ") AS " + labelCol + ", " + StringUtils.join(otherColumns, ",") + " FROM data");

		df2.printSchema();
		return df2.distinct();
	}
	public static void main(String[] args) {
		theLogger.setLevel(Level.INFO);
		FileHandler logFile;
		try {
			logFile = new FileHandler("BinaryClassifierDTPredict.log");
			logFile.setFormatter(new SimpleFormatter());
			theLogger.addHandler(logFile);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PipelineModel dt_category_model = new PipelineModel.PipelineModelReader().load("");
		SparkConf conf = new SparkConf().setAppName("Dicision Tree Prediction").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);

		String[] categoryFeatures = new String[] { "LOGICAL_NAME", "SUBCATEGORY", "PRODUCT_TYPE", "DEPT", "HP_ISSUE_TYPE_ID", "HP_PROD_SPEC_ID", "ASSIGNMENT" };
		String[] otherFeatures = new String[]{"INCIDENT_ID"}; 
		DataFrame df = loadFromParquet(sqlContext, "ml_data/e-mail exchange infrastructure.parquet", labelCol, categoryFeatures, otherFeatures);
		df.cache();

		df.printSchema();
		df.show(10);


	}

}
