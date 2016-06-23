package com.hpe.sm.util;

import org.apache.spark.sql.DataFrame;

public class DataPrepare {
	public static long[] statsOnLabel(DataFrame data, String labelCol) {
		long posCnt = data.filter(labelCol + "=1.0").count();
		long negCnt = data.filter(labelCol + "=0.0").count();
		// theLogger.info("stats: total amout:" + data.count());
		// theLogger.info("stats: positive amout:" + posCnt);
		// theLogger.info("stats: negative amout:" + negCnt);
		return new long[] { posCnt, negCnt };

	}

	public static DataFrame[] split_data(DataFrame data, double ratio, boolean balance_data, String labelCol) {
		long[] cnts = statsOnLabel(data, labelCol);
		double posCnt = cnts[0] * 1.0;
		double negCnt = cnts[1] * 1.0;
		double pn_ratio = posCnt * 1.0 / negCnt;
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
			return new DataFrame[] { splits[0], splits[1].unionAll(left_data) };
		}

		DataFrame[] splits = data.randomSplit(new double[] { ratio, 1.0 - ratio }, 12345);
		return splits;
	}
}

