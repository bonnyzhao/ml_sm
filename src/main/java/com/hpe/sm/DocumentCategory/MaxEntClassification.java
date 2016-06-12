package com.hpe.sm.DocumentCategory;

import java.util.List;



import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.hpe.sm.train.Change;

public class MaxEntClassification {
	private static String[] CATEGORIES = { "pos", "neg" };
	private static int NGRAM_SIZE = 1;
	private static DynamicLMClassifier classifier = DynamicLMClassifier.createNGramBoundary(CATEGORIES, NGRAM_SIZE);
	
	public static void train(Change change){
		String category = change.getResult()? "pos" : "neg";
		List<String> features = change.getFeatures();
		String featureStr = "";
		for(String s : features){
			featureStr += s.replaceAll("/s", "_") + " ";
		}
		CharSequence sequence = featureStr;
		
		classifier.train(category, sequence, 1);
	}
	
	public static double test(Change change){
		CharSequence sequence = change.getDescription();
		JointClassification result = classifier.classify(sequence);
		if(result.bestCategory().equals("neg")){
			return result.conditionalProbability(0) * -1;
		}
		return result.conditionalProbability(0);
		
	}
}
