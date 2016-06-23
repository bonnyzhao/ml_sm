package com.hpe.sm.sla;

import java.util.ArrayList;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.Model;
import org.apache.spark.mllib.linalg.DenseVector;

public class binaryTest {
	public static DataFrame binaryClassificationMetrics(DataFrame predictions, String labelCol, String predictionCol) {
		DataFrame tp = predictions.groupBy(labelCol, predictionCol).count();
		return tp;
	}
	public static DataFrame binaryClassificationProbabilityMetrics(DataFrame predictions, SQLContext sc, String labelCol, String predictionCol, String probabilityCol) {
		//predictions.registerTempTable("predictions");
		// Generate the schema based on the string of schema
		ArrayList<StructField> fields = new ArrayList<StructField>();
		for (String fieldName: new String[]{predictionCol, labelCol, "p_probability"}) {
		  fields.add(DataTypes.createStructField(fieldName, DataTypes.DoubleType, true));
		}
		StructType schema = DataTypes.createStructType(fields);

		//predictions = sc.sql("SELECT *, explode(probability) [1] as p_probability  FROM predictions");
		JavaRDD modifiedRDD = predictions.toJavaRDD().map(new Function<Row, Row>(){

			public Row call(Row v1) throws Exception {
				if(v1!=null){
				double p_probability = ((DenseVector)v1.get(2)).values()[1];
				
				return RowFactory.create(v1.getDouble(0), v1.getDouble(1), p_probability);
				}else{
					return null;
				}
			}
			
		});
		predictions = sc.createDataFrame(modifiedRDD, schema);
		System.out.println("new schema:");
		predictions.printSchema();
		System.out.println("after cleaning:");
		predictions.show(10);
		DataFrame tp = predictions.groupBy(labelCol, predictionCol).mean("p_probability");
		return tp;
	}
	
	public static String testModelMetrics(Model model, DataFrame test, boolean withPossibility, SQLContext sc, String labelCol, String predictionCol, String probabilityCol) {
		String result = "";
		// Make predictions on test documents.
		DataFrame predictions = model.transform(test).select(predictionCol, labelCol, probabilityCol);
		//theLogger.info("10 predictions");
		//theLogger.info(predictions.showString(10, false));
		//predictions.filter(labelCol + "=1.0").filter("prediction = 1.0").
		DataFrame metrics = binaryClassificationMetrics(predictions, labelCol,  predictionCol);
		
		DataFrame tp_row = metrics.filter(labelCol + "= 1.0  and " + predictionCol + " = 1.0");
		long tp = tp_row.collect().length>0 ? tp_row.collect()[0].getLong(2) : 0;
		
		DataFrame fp_row = metrics.filter(labelCol + "= 0.0  and " + predictionCol + " = 1.0");
		long fp = fp_row.collect().length>0 ? fp_row.collect()[0].getLong(2) : 0;
		
		DataFrame fn_row = metrics.filter(labelCol + "= 1.0  and " + predictionCol + " = 0.0");
		long fn = fn_row.collect().length>0 ? fn_row.collect()[0].getLong(2) : 0;

		DataFrame tn_row = metrics.filter(labelCol + "= 0.0  and " + predictionCol + " = 0.0");
		long tn = tn_row.collect().length>0 ? tn_row.collect()[0].getLong(2) : 0;
		
		result += "testing metrics:\n" + metrics.showString(5, false);
		if(withPossibility){
			System.out.println("predictions schema");
			predictions.printSchema();
			DataFrame prob_metrics = binaryClassificationProbabilityMetrics(predictions, sc, labelCol, predictionCol, probabilityCol);
			result += "testing metrics(probablity average):\n" +  prob_metrics.showString(5, false);
		}
		result += "P\\L\t True\t False\t\n";
		result += "True\t " + tp + "\t" + fp + "\t\n";
		result += "False\t " + fn + "\t" + tn + "\t\n";

		long total = test.count(); 
		double accuracy = 1.0*(tp+tn)/total;
		double precision = 1.0*tp/(tp+fp);
		double recall = 1.0*tp/(tp+fn);
		
		result += "accuracy:" + accuracy+"\n";
		result += "precision:" + precision+"\n";
		result += "recall:" + recall+"\n";
		return result;
	}
	
	public static String testModel(Model model, DataFrame test, String labelCol, String predictionCol, String probabilityCol) {
		String result = "";
		// Make predictions on test documents.
		DataFrame predictions = model.transform(test).select(predictionCol, labelCol, probabilityCol);
		//BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator().setLabelCol(labelCol).setRawPredictionCol("probability").setMetricName("areaUnderROC");
		//double aucTesting = evaluator.evaluate(predictions);
		//result += "testing auc:" + aucTesting;
		
		//result += "testing metrics:\n" + testModelMetrics(model,test);
		//theLogger.info(result);
		return result;
	}
}
