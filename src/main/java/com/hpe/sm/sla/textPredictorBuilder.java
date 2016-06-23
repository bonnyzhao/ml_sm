package com.hpe.sm.sla;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.spark.sql.functions.*;
import org.apache.spark.sql.api.java.UDF1;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Model;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.LogisticRegressionTrainingSummary;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.param.Params;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.GroupedData;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.*;

public class textPredictorBuilder {
	public static final String textCol = "text";
	public static final String labelCol = "label";
	public static final String predictionCol = "prediction_text";
	public static final String probabilityCol = "probability_text";

	private static final int MinDocFreq = 5;
	private static Logger theLogger = Logger.getLogger(textPredictorBuilder.class.getName());

	public static DataFrame loadFromParquet(SQLContext sc, String filePath, String labelColumn, String[] textColumns,
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
		ArrayList<String> tmp = new ArrayList<String>();
		for (String text : textColumns) {
			tmp.add(text);
			tmp.add("' '");
		}

		DataFrame df2 = sc.sql("SELECT CONCAT(" + StringUtils.join(tmp.toArray(new String[] {}), ",") + ") as "
				+ textCol + ",  label_converter(" + labelColumn + ") AS " + labelCol + ", "
				+ StringUtils.join(otherColumns, ",") + " FROM data");

		df2.printSchema();
		return df2.distinct();
	}

	public static long[] statsOnLabel(DataFrame data) {
		long posCnt = data.filter(labelCol + "=1.0").count();
		long negCnt = data.filter(labelCol + "=0.0").count();
		theLogger.info("stats: total amout:" + data.count());
		theLogger.info("stats: positive amout:" + posCnt);
		theLogger.info("stats: negative amout:" + negCnt);
		return new long[] { posCnt, negCnt };

	}

	public static DataFrame[] split_data(DataFrame data, double ratio, boolean balance_data, long max_train_number,
			long max_test_number) {
		long[] cnts = statsOnLabel(data);
		double posCnt = cnts[0] * 1.0;
		double negCnt = cnts[1] * 1.0;
		double pn_ratio = posCnt * 1.0 / negCnt;
		DataFrame training_df = null;
		DataFrame test_df = null;
		if (balance_data) {
			DataFrame positive = data.filter(labelCol + "=1.0");
			DataFrame negative = data.filter(labelCol + "=0.0");
			DataFrame balanced_data;
			DataFrame left_data;
			if (pn_ratio > 1.0) {
				double tmp = negCnt / posCnt;
				DataFrame[] pos_splits = positive.randomSplit(new double[] { tmp, 1.0 - tmp }, 12345);
				balanced_data = pos_splits[0].unionAll(negative);
				left_data = pos_splits[1];
			} else {
				DataFrame[] neg_splits = negative.randomSplit(new double[] { pn_ratio, 1.0 - pn_ratio }, 12345);
				balanced_data = neg_splits[0].unionAll(positive);
				left_data = neg_splits[1];
			}
			DataFrame[] splits = balanced_data.randomSplit(new double[] { ratio, 1.0 - ratio }, 12345);
			training_df = splits[0];
			test_df = splits[1].unionAll(left_data);
		} else {
			DataFrame[] splits = data.randomSplit(new double[] { ratio, 1.0 - ratio }, 12345);
			training_df = splits[0];
			test_df = splits[1];
		}
		if (training_df.count() > max_train_number) {
			ratio = 1.0 * max_train_number / training_df.count();
			training_df = training_df.randomSplit(new double[] { ratio, 1.0 - ratio })[0];
		}
		if (test_df.count() > max_test_number) {
			ratio = 1.0 * max_test_number / test_df.count();
			test_df = test_df.randomSplit(new double[] { ratio, 1.0 - ratio })[0];
		}
		return new DataFrame[] { training_df, test_df };
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

	public static DataFrame binaryClassificationMetrics(DataFrame predictions) {
		/*
		 * predictions.registerTempTable("predictions"); DataFrame pos =
		 * sqlContext.sql(
		 * "SELECT prediction, label, probability FROM predictions "); for (Row
		 * r : pos.select("label", "prediction", "probability").take(300)) {
		 * System.out.println("(" + r.get(0) + ", " + r.get(1) + ", " + r.get(2)
		 * + ")"); }
		 */
		DataFrame tp = predictions.groupBy("label", "prediction").count();
		System.out.println("groupby:");
		tp.show();
		return tp;
	}

	public static PipelineModel lrTextFeatureModel(DataFrame training) {

		RegexTokenizer regexTokenizer = new RegexTokenizer().setInputCol(textCol).setOutputCol("words")
				.setPattern("\\W");
		StopWordsRemover remover = new StopWordsRemover().setInputCol(regexTokenizer.getOutputCol())
				.setOutputCol("filtered_words");

		HashingTF hashingTF = new HashingTF().setNumFeatures(1000).setInputCol(remover.getOutputCol())
				.setOutputCol("rawFeatures");

		IDF idf = new IDF().setInputCol(hashingTF.getOutputCol()).setOutputCol("textFeatures")
				.setMinDocFreq(MinDocFreq);

		LogisticRegression lr = new LogisticRegression();
		// theLogger.info("LogisticRegression parameters:\n" +
		// lr.explainParams() + "\n");

		lr.setMaxIter(50).setRegParam(0.01).setLabelCol(labelCol).setFeaturesCol(idf.getOutputCol())
				.setProbabilityCol(probabilityCol).setPredictionCol(predictionCol);// .setElasticNetParam(0.8);

		Pipeline pipeline = new Pipeline()
				.setStages(new PipelineStage[] { regexTokenizer, remover, hashingTF, idf, lr });

		PipelineModel model = pipeline.fit(training);
		return model;
	}

	public static Model lrTextFeatureModel_cross(DataFrame training) {

		RegexTokenizer regexTokenizer = new RegexTokenizer().setInputCol(textCol).setOutputCol("words")
				.setPattern("\\W");
		StopWordsRemover remover = new StopWordsRemover().setInputCol(regexTokenizer.getOutputCol())
				.setOutputCol("filtered_words");

		HashingTF hashingTF = new HashingTF().setNumFeatures(100).setInputCol(remover.getOutputCol())
				.setOutputCol("rawFeatures");

		IDF idf = new IDF().setInputCol(hashingTF.getOutputCol()).setOutputCol("textFeatures")
				.setMinDocFreq(MinDocFreq);

		LogisticRegression lr = new LogisticRegression();
		// theLogger.info("LogisticRegression parameters:\n" +
		// lr.explainParams() + "\n");

		lr.setMaxIter(10).setRegParam(0.01).setLabelCol("label").setFeaturesCol(idf.getOutputCol())
				.setProbabilityCol(probabilityCol).setPredictionCol(predictionCol).setElasticNetParam(0.8);

		Pipeline pipeline = new Pipeline()
				.setStages(new PipelineStage[] { regexTokenizer, remover, hashingTF, idf, lr });

		ParamMap[] paramGrid = new ParamGridBuilder().addGrid(hashingTF.numFeatures(), new int[] { 200, 500 })
				// .addGrid(lr.regParam(), new double[] { 1.0, 0.1 })
				.addGrid(lr.maxIter(), new int[] { 100, 200 }).build();

		BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator().setLabelCol("label")
				.setRawPredictionCol("probability").setMetricName("areaUnderROC");

		CrossValidator cv = new CrossValidator().setNumFolds(2).setEstimator(pipeline).setEstimatorParamMaps(paramGrid)
				.setEvaluator(evaluator);

		CrossValidatorModel model = cv.fit(training);
		return model;
		// return model.bestModel();
	}

	public static void writeStats2File(String path, String stats) throws IOException {
		FileWriter fw = new FileWriter(path);

		fw.write(stats);

		fw.close();
	}

	public static void main(String[] args) throws IOException {

		// TODO Auto-generated method stub
		SparkConf conf = new SparkConf().setAppName("Linear Regression Example").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		String[] textColumns = new String[] { "TITLE", "DESCRIPTION" };
		String[] otherColumns = new String[] { "INCIDENT_ID" };
		// INCIDENT_ID OWNER_NAME AFFECTED_ITEM LOGICAL_NAME TITLE SUBCATEGORY
		// PRODUCT_TYPE DEPT HP_ISSUE_TYPE_ID HP_PROD_SPEC_ID ASSIGNMENT BREACH

		// Register the DataFrame as a table.
		// String[] affected_items = new String[]{"e-mail exchange
		// infrastructure" , "remote access to hp-client support", "remote
		// access to hp-client support", "mcafeeendpoint encryption",
		// "lync-instant messaging", "nw-dns support" };
		String[] affected_items = new String[] { "mcafee endpoint encryption",
				"lync-instant messaging", "nw-dns support" };
		for (String item : affected_items) {
			// DataFrame df = loadFromParquet(sqlContext, "ml_data/e-mail
			// exchange infrastructure.parquet", "SLA_BREACH", textColumns,
			// otherColumns);
			System.out.println("ml_data/" + item + ".parquet");
			DataFrame df = loadFromParquet(sqlContext, "ml_data/" + item + ".parquet", "SLA_BREACH", textColumns,
					otherColumns);
			df.cache();
			DataFrame[] splits = split_data(df, 0.8, true, 5000, 2000);
			DataFrame training = splits[0];
			DataFrame test = splits[1];
			String stats = "";

			long[] stats_training = statsOnLabel(training);
			long[] stats_test = statsOnLabel(test);

			stats += "data stats:\n";
			stats += "data\t\tpositive\t\tnegativ\n";
			stats += "training\t\t" + stats_training[0] + "\t\t" + stats_training[1] + "\n";
			stats += "testing\t\t" + stats_test[0] + "\t\t" + stats_test[1] + "\n";

			PipelineModel model = lrTextFeatureModel(training);
			try {
				model.write().overwrite().save("ml_models/textSLAPredictor_" + item);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Make predictions on test documents.
			// DataFrame predictions =
			// model.transform(test).select("prediction", "label",
			// "probability");
			String test_metrix = binaryTest.testModelMetrics(model, df, true, sqlContext, labelCol, predictionCol,
					probabilityCol);
			System.out.println("testing metrix:");
			System.out.println(test_metrix);

			stats += test_metrix;

			writeStats2File("ml_models/textSLAPredictor_" + item + "_stats.txt", stats);
			theLogger.info("testing metrix:\n" + test_metrix);
		}
	}

}
