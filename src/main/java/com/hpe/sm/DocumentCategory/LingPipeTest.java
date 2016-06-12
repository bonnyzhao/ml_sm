package com.hpe.sm.DocumentCategory;


import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.hpe.sm.train.Change;

public class LingPipeTest {
	private static String[] CATEGORIES = { "pos", "neg" };
	private static int NGRAM_SIZE = 2;
	private static DynamicLMClassifier classifier = DynamicLMClassifier.createNGramBoundary(CATEGORIES, NGRAM_SIZE);
	
	public LingPipeTest(){
		//DynamicLMClassifier classifier = DynamicLMClassifier.createNGramBoundary(CATEGORIES, NGRAM_SIZE);
	}
	
	public static void train(Change change){
		String category = change.getResult()? "pos" : "neg";
		
//		String features = "";
//		for(String s : change.getFeatures()){
//			features += s.replace("[^a-zA-Z0-9]", "_") + " ";
//		}
//		CharSequence sequence = features + " " + change.getDescription().replaceAll("[^a-zA-Z0-9]", " ");
		CharSequence sequence = change.getDescription().replaceAll("[^a-zA-Z0-9]", " ");
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
