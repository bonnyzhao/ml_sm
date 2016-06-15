package com.hpe.sm.DocumentCategory;


import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.spell.JaccardDistance;
import com.aliasi.tokenizer.NGramTokenizerFactory;
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
		CharSequence sequence = change.getDescription().replaceAll("[^a-zA-Z0-9]", " ");
		JointClassification result = classifier.classify(sequence);
		if(result.bestCategory().equals("neg")){
			return result.conditionalProbability(0) * -1;
		}
		return result.conditionalProbability(0);
		
	}
	
	public static double distance(Change change1, Change change2){
		CharSequence sequence1 = change1.getDescription().replaceAll("[^a-zA-Z0-9]", " ");
		CharSequence sequence2 = change2.getDescription().replaceAll("[^a-zA-Z0-9]", " ");

		JaccardDistance jd = new JaccardDistance(new NGramTokenizerFactory(1, 2));//bigram
		double disDescription = jd.distance(sequence1, sequence2);
		return disDescription;
		
	}
	
}
