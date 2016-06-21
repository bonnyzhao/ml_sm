package com.hpe.sm.DocumentCategory;


import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.spell.JaccardDistance;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.hpe.sm.train.Change;

public class RiskEstimation {
	private static String[] CATEGORIES = { "pos", "neg" };
	private static int NGRAM_SIZE = 2;
	private static DynamicLMClassifier classifier = DynamicLMClassifier.createNGramBoundary(CATEGORIES, NGRAM_SIZE);
	
	public RiskEstimation(){
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
		return test(change.getDescription());
		
	}
	
	public static double test(String desc){
		desc = desc.replaceAll("[^a-zA-Z0-9]", " ");
		JointClassification result = classifier.classify(desc);
		if(result.bestCategory().equals("neg")){
			return result.conditionalProbability(0) * -1;
		}
		return result.conditionalProbability(0);
		
	}
	
	public static double distance(String desc1, String desc2){
		desc1 = desc1.replaceAll("[^a-zA-Z0-9]", " ");
		desc2 = desc2.replaceAll("[^a-zA-Z0-9]", " ");

		JaccardDistance jd = new JaccardDistance(new NGramTokenizerFactory(1, 2));//bigram
		double disDescription = jd.distance(desc1, desc2);
		return disDescription;
		
	}
	
}
