package org.apache.spark.examples.ml;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.ml.feature.MinMaxScaler;
import org.apache.spark.ml.feature.MinMaxScalerModel;
// $example on$
import org.apache.spark.ml.feature.Normalizer;
import org.apache.spark.sql.DataFrame;

// $example off$
public class MinMaxScalerExample {
	public static void main(String[] args) {
	    SparkConf conf = new SparkConf().setAppName("JavaNormalizerExample").setMaster("local");
	    JavaSparkContext jsc = new JavaSparkContext(conf);
	    SQLContext jsql = new SQLContext(jsc);

	    // $example on$
	    DataFrame dataFrame = jsql.read().format("libsvm").load("C:/spark-1.6.1-bin-hadoop2.6/spark-1.6.1-bin-hadoop2.6/data/mllib/sample_libsvm_data.txt");
		MinMaxScaler scaler = new MinMaxScaler().setInputCol("features").setOutputCol("scaledFeatures");

		// Compute summary statistics and generate MinMaxScalerModel
		MinMaxScalerModel scalerModel = scaler.fit(dataFrame);

		// rescale each feature to range [min, max].
		DataFrame scaledData = scalerModel.transform(dataFrame);
		scaledData.show();
	}
}