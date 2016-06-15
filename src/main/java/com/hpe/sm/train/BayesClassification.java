package com.hpe.sm.train;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import src.main.java.de.daslaboratorium.machinelearning.classifier.Classification;
import src.main.java.de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;

import com.hpe.sm.DocumentCategory.LingPipeTest;
import com.hpe.sm.DocumentCategory.MaxEntClassification;
import com.hpe.sm.svm.SVMTrain;

import libsvm.svm_model;

public class BayesClassification {
	private static BayesClassifier<String, Boolean> bayes =
            new BayesClassifier<String, Boolean>();
	private static List<Change> changes = Read.getInfo();
	private static float trainPercent = 0.5f;
	private static List<Change> trainSet = new ArrayList<Change>();
	private static List<Change> testSet = new ArrayList<Change>();
	
//	public static void main(String[] args){
//		 train();
//		 test();
//	}
	
	public static void getTrainTestSet(){
		trainSet.clear();
		testSet.clear();
		
		for(Change c : changes){
			if(Math.random() > trainPercent){
				testSet.add(c);
			}
			//all data is put into trainset
			trainSet.add(c);
			if(trainSet.size() > 5000) break;
		}
//		int count = 0;
//		List<Change> balancedTrainSet = new ArrayList<Change>();
//		for(int i = 0; i < trainSet.size(); ++i){
//			if(trainSet.get(i).getResult()){
//				balancedTrainSet.add(trainSet.get(i));
//				++count;
//				if(count == 500){
//					break;
//				}
//			}
//		}
//		count = 0;
//		for(int i = 0; i < trainSet.size(); ++i){
//			if(!trainSet.get(i).getResult()){
//				balancedTrainSet.add(trainSet.get(i));
//				++count;
//				if(count == 500){
//					break;
//				}
//			}
//		}
//		
// 		trainSet = balancedTrainSet;
	}
	
	public static void train(){
		if(trainSet.size() == 0) getTrainTestSet();
		bayes.setMemoryCapacity(trainSet.size());
		LingPipeTest lingPipe = new LingPipeTest();
		
		for(Change c : trainSet){
			bayes.learn(c.getResult(), c.getFeatures());
			MaxEntClassification.train(c);
			lingPipe.train(c);
		}
		//Adaboost adaboost = new Adaboost(bayes, lingPipe, testSet);
		//adaboost.train();
		
	}
	
	public static void test(){
		int[] bayesSta = {0, 0, 0, 0};
		int[] maxEntSta = {0, 0, 0, 0};
		int[] combineSta = {0, 0, 0, 0};
		
		List<Boolean> correctResult = new ArrayList<Boolean>();
		List<Float> bayesPercentageList = new ArrayList<Float>();
		List<Float> maxEntPercentageList = new ArrayList<Float>();
		List<Float> neighbourList = new ArrayList<Float>();
		
		for(Change c : testSet){
			double maxEntPercentage = LingPipeTest.test(c);
			boolean maxEntResult = maxEntPercentage > 0? true : false;
			
			boolean bayesResult;// = bayes.classify(c.getFeatures()).getCategory();
			double bayesPercentage;
			
			Iterator<Classification<String, Boolean>> it = bayes.classifyDetailed(c.getFeatures()).iterator();
			Classification<String, Boolean> firstPos = (Classification<String, Boolean>)it.next();
			Classification<String, Boolean> secondPos = (Classification<String, Boolean>)it.next();
			if(firstPos.getProbability() > secondPos.getProbability()){
				bayesResult = firstPos.getCategory();
				bayesPercentage = firstPos.getProbability() / (firstPos.getProbability() + secondPos.getProbability());
				//bayesPercentage = firstPos.getProbability();
			}else{
				bayesResult = secondPos.getCategory();
				bayesPercentage = secondPos.getProbability() / (firstPos.getProbability() + secondPos.getProbability());
				//bayesPercentage = secondPos.getProbability();
			}
			bayesPercentage = bayesResult? bayesPercentage : bayesPercentage * -1;
			
			//double featuredPercentage = MaxEntClassification.test(c);
			//boolean featuredResult = bayesPercentage > 0? true : false;
			
			correctResult.add(c.getResult());
			bayesPercentageList.add((float)bayesPercentage);
			maxEntPercentageList.add((float)maxEntPercentage);
			//featuredClassification.add((float)featuredPercentage);
			
			boolean combinedResult = //maxEntResult;
					1.0839264057606783 * bayesPercentage + 0.2770613798522845 * maxEntPercentage + -0.19389341440327365 > 0? 
							true : false;
			
			if(combinedResult == true && c.getResult() == true) ++combineSta[0];//tp
			if(combinedResult == false && c.getResult() == true) ++combineSta[1];//fn
			if(combinedResult == true && c.getResult() == false) ++combineSta[2];//fp
			if(combinedResult == false && c.getResult() == false) ++combineSta[3];//tn
			
			if(bayesResult == true && c.getResult() == true) ++bayesSta[0];
			if(bayesResult == false && c.getResult() == true) ++bayesSta[1];
			if(bayesResult == true && c.getResult() == false) ++bayesSta[2];
			if(bayesResult == false && c.getResult() == false) ++bayesSta[3];
			
			if(maxEntResult == true && c.getResult() == true) ++maxEntSta[0];
			if(maxEntResult == false && c.getResult() == true) ++maxEntSta[1];
			if(maxEntResult == true && c.getResult() == false) ++maxEntSta[2];
			if(maxEntResult == false && c.getResult() == false) ++maxEntSta[3];
			
			//considering neighbours
			Change[] neighbour = findNeighbour(c, 10);
			float trueCount = 0;
			for(Change n : neighbour){
				trueCount += n.getResult()? 1 : 0;
			}
			neighbourList.add((trueCount / 10) * 2 - 1);
		}
		
		double accuracy = (double)(combineSta[0] + combineSta[3]) / testSet.size();
		double pricision = (double)combineSta[0] / (combineSta[0] + combineSta[2]);
		double recall = (double)combineSta[0] / (combineSta[0] + combineSta[1]);
		System.out.println("Combined: ");
		System.out.println("Accuracy: " + accuracy);
		System.out.println("Pricision: " + pricision);
		System.out.println("Recall: " + recall);
		System.out.println("F1: " + 2 * recall * accuracy / (recall + accuracy));
		System.out.println("tp: " + combineSta[0] + "\ttn: " + combineSta[3] + "\tfp: " + combineSta[2] + "\tfn: " + combineSta[1]);
		
		accuracy = (double)(bayesSta[0] + bayesSta[3]) / testSet.size();
		pricision = (double)bayesSta[0] / (bayesSta[0] + bayesSta[2]);
		recall = (double)bayesSta[0] / (bayesSta[0] + bayesSta[1]);
		System.out.println("Bayes: ");
		System.out.println("Accuracy: " + accuracy);
		System.out.println("Pricision: " + pricision);
		System.out.println("Recall: " + recall);
		System.out.println("F1: " + 2 * recall * accuracy / (recall + accuracy));
		System.out.println("tp: " + bayesSta[0] + "\ttn: " + bayesSta[3] + "\tfp: " + bayesSta[2] + "\tfn: " + bayesSta[1]);
		
		accuracy = (double)(maxEntSta[0] + maxEntSta[3]) / testSet.size();
		pricision = (double)maxEntSta[0] / (maxEntSta[0] + maxEntSta[2]);
		recall = (double)maxEntSta[0] / (maxEntSta[0] + maxEntSta[1]);
		System.out.println("Max Entropy: ");
		System.out.println("Accuracy: " + accuracy);
		System.out.println("Pricision: " + pricision);
		System.out.println("Recall: " + recall);
		System.out.println("F1: " + 2 * recall * accuracy / (recall + accuracy));
		System.out.println("tp: " + maxEntSta[0] + "\ttn: " + maxEntSta[3] + "\tfp: " + maxEntSta[2] + "\tfn: " + maxEntSta[1]);
		
		
		
		
		svm_model model = SVMTrain.train(correctResult, bayesPercentageList, maxEntPercentageList, neighbourList);
	}
	
	public static ChangeImpactResult test(Change change){
		ChangeImpactResult result = new ChangeImpactResult();
		
		double bayesPercentage;
		boolean bayesResult;
		Iterator<Classification<String, Boolean>> it = bayes.classifyDetailed(change.getFeatures()).iterator();
		Classification<String, Boolean> firstPos = (Classification<String, Boolean>)it.next();
		Classification<String, Boolean> secondPos = (Classification<String, Boolean>)it.next();
		if(firstPos.getProbability() > secondPos.getProbability()){
			bayesResult = firstPos.getCategory();
			bayesPercentage = firstPos.getProbability() / (firstPos.getProbability() + secondPos.getProbability());
			System.out.println("1st: " + firstPos.getCategory() + "\t" + firstPos.getProbability());
			System.out.println("2nd: " + secondPos.getCategory() + "\t" + secondPos.getProbability());
			//bayesPercentage = firstPos.getProbability();
		}else{
			bayesResult = secondPos.getCategory();
			bayesPercentage = secondPos.getProbability() / (firstPos.getProbability() + secondPos.getProbability());
			System.out.println("1st: " + firstPos.getCategory() + "\t" + firstPos.getProbability());
			System.out.println("2nd: " + secondPos.getCategory() + "\t" + secondPos.getProbability());
			//bayesPercentage = secondPos.getProbability();
		}
		bayesPercentage = bayesResult? bayesPercentage : bayesPercentage * -1;
		
		double maxEntPercentage = LingPipeTest.test(change);
		
		double combinedResult = 1.0839264057606783 * bayesPercentage + 0.2770613798522845 * maxEntPercentage + -0.19389341440327365;
		//double combinedResult = 0.8061453348032956 * bayesPercentage + 0.2626419275940862 * maxEntPercentage + -0.2706269200074364;
				
		result.setPossibility((combinedResult + 1) / 2);
		
		result.setRelatedChanges(findNeighbour(change, 5));
		
		return result;
	}
	
	private static Change[] findNeighbour(Change change, int length){
		double[] distances = new double[length];
		Change[] changes = new Change[length];
		for(int i = 0; i < length; ++i){
			distances[i] = Double.MAX_VALUE;
		}
		for (int i = 0; i < trainSet.size(); ++i) {
			double d = LingPipeTest.distance(change, trainSet.get(i));
			double common = 1;
			for (String s : change.getFeatures()) {
				if (trainSet.get(i).getFeatures().contains(s)) {
					if (s.contains("AssignGroup") || s.contains("Coordinator")) {
						common += 0.05;
					} else if (s.startsWith("Gl")) {
						common += 0.3;
					} else {
						common += 0.2;
					}
				}

			}
			d = d / common;
			if (d < distances[length - 1]
					&& !change.getID().equals(trainSet.get(i).getID())) {
				if (d > distances[length - 2]) {
					distances[length - 1] = d;
					changes[length - 1] = trainSet.get(i);
				} else {
					int index = length - 2;
					while (index > 0 && d < distances[index - 1]) {
						--index;
					}
					for (int j = length - 1; j > index; --j) {
						distances[j] = distances[j - 1];
						changes[j] = changes[j - 1];
					}
					distances[index] = d;
					changes[index] = trainSet.get(i);

				}
			}
		}
		return changes;
	}
}
