package com.hpe.sm.DocumentCategory;

import java.util.ArrayList;
import java.util.List;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.hpe.sm.restfulApi.AttributesUnit;
import com.hpe.sm.train.Change;

/**
 * @author zhu-jing.wu@hpe.com
 * @date Jun 18, 2016 9:02:24 PM
 */
public class DescriptionCategory {
	//private static String[] CATEGORIES;
	private static int NGRAM_SIZE = 2;
	private static DynamicLMClassifier assignGroupClassifier;
	private static DynamicLMClassifier coorditorClassifier;
	private static DynamicLMClassifier affectedCiClassifier;
	private static DynamicLMClassifier categoryClassifier;
	private static DynamicLMClassifier modelClassifier;
	
	public DescriptionCategory(){
		
	}
	
	public static void train(List<Change> trainSet){
		//go through the whole set to get categories
		List<String> assignGourpCate = new ArrayList<String>();
		List<String> coorditorCate = new ArrayList<String>();
		List<String> affectedCiCate = new ArrayList<String>();
		List<String> categoryCate = new ArrayList<String>();
		List<String> modelpCate = new ArrayList<String>();
		for(Change change : trainSet){
			for(String s : change.getFeatures()){
				String category = "";
				if(s.startsWith("AssignGroup ")){
					category = s.substring("AssignGroup ".length());
					if(!assignGourpCate.contains(category)) assignGourpCate.add(category);
				}else if(s.startsWith("Coordinator ")){
					category = s.substring("Coordinator ".length());
					if(!coorditorCate.contains(category)) coorditorCate.add(category);
				}else if(s.startsWith("Logical ")){
					category = s.substring("Logical ".length());
					if(!affectedCiCate.contains(category)) affectedCiCate.add(category);
				}else if(s.startsWith("Category ")){
					category = s.substring("Category ".length());
					if(!categoryCate.contains(category)) categoryCate.add(category);
				}else if(s.startsWith("Gl ")){
					category = s.substring("Gl ".length());
					if(!modelpCate.contains(category)) modelpCate.add(category);
				}
			}
		}
		System.out.println("Assigned Group: " + assignGourpCate.size());
		System.out.println("Coordinator: " + coorditorCate.size());
		System.out.println("Affected CI: " + affectedCiCate.size());
		System.out.println("Model: " + modelpCate.size());
		System.out.println("Category: " + categoryCate.size() + "\r\n");
		
		String[] categoryArr = new String[assignGourpCate.size()];
		assignGroupClassifier = DynamicLMClassifier.createNGramBoundary(
				assignGourpCate.toArray(categoryArr), NGRAM_SIZE);
		categoryArr = new String[coorditorCate.size()];
		coorditorClassifier = DynamicLMClassifier.createNGramBoundary(
				coorditorCate.toArray(categoryArr), NGRAM_SIZE);
		categoryArr = new String[affectedCiCate.size()];
		affectedCiClassifier = DynamicLMClassifier.createNGramBoundary(
				affectedCiCate.toArray(categoryArr), NGRAM_SIZE);
		categoryArr = new String[categoryCate.size()];
		categoryClassifier = DynamicLMClassifier.createNGramBoundary(
				categoryCate.toArray(categoryArr), NGRAM_SIZE);
		categoryArr = new String[modelpCate.size()];
		modelClassifier = DynamicLMClassifier.createNGramBoundary(
				modelpCate.toArray(categoryArr), NGRAM_SIZE);
		
		//train models		
		for(Change change : trainSet){
			for(String s : change.getFeatures()){
				String category = "";
				CharSequence sequence = change.getDescription().replaceAll("[^a-zA-Z0-9]", " ");
				if(s.startsWith("AssignGroup ")){
					category = s.substring("AssignGroup ".length());
					assignGroupClassifier.train(category, sequence, 1);
				}else if(s.startsWith("Coordinator ")){
					category = s.substring("Coordinator ".length());
					coorditorClassifier.train(category, sequence, 1);
				}else if(s.startsWith("Logical ")){
					category = s.substring("Logical ".length());
					affectedCiClassifier.train(category, sequence, 1);
				}else if(s.startsWith("Category ")){
					category = s.substring("Category ".length());
					categoryClassifier.train(category, sequence, 1);
				}else if(s.startsWith("Gl ")){
					category = s.substring("Gl ".length());
					modelClassifier.train(category, sequence, 1);
				}
			}
		}
		
		
	}
	
	public static AttributesUnit[] test(String description, int num){
		CharSequence sequence = description.replaceAll("[^a-zA-Z0-9]", " ");
		//5 categories and 1 risk
		AttributesUnit[] result = new AttributesUnit[6];
		
		//assign group
		String[] categories = new String[num];
		JointClassification rawResult = assignGroupClassifier.classify(sequence);
		for(int i = 0; i < num; ++i){
			categories[i] = rawResult.category(i);
		}
		result[0] = new AttributesUnit(
				"assignment.group", "Assignment Group", "combo", 
				categories, categories);
		//coordinator
		rawResult = coorditorClassifier.classify(sequence);
		for(int i = 0; i < num; ++i){
			categories[i] = rawResult.category(i);
		}
		result[1] = new AttributesUnit(
				"cordinator", "Cordinator", "combo"
				, categories, categories);
		//affected service
		rawResult = affectedCiClassifier.classify(sequence);
		for(int i = 0; i < num; ++i){
			categories[i] = rawResult.category(i);
		}
		result[2] = new AttributesUnit(
				"affected.item", "Affected CI", "combo",
				categories, categories);
		//change model
		rawResult = modelClassifier.classify(sequence);
		for(int i = 0; i < num; ++i){
			categories[i] = rawResult.category(i);
		}
		result[3] = new AttributesUnit(
				"model", "Change Model", "combo", categories,
				 categories);
		//category
		rawResult = categoryClassifier.classify(sequence);
		for(int i = 0; i < 3; ++i){
			categories[i] = rawResult.category(i);
		}
		categories[3] = "";
		categories[4] = "";
		result[4] = new AttributesUnit(
				"category", "Category", "combo", categories,
				 categories);
		return result;
	}
	
	public static void test(List<Change> testSet){
		double assignGroupCount = 0;
		double coordinatorCount = 0;
		double affectedCiCount = 0;
		double categoryCount = 0;
		double modelCount = 0;
		int num = 5;
		int count = 1;
		for(Change c : testSet){
			CharSequence sequence = c.getDescription().replaceAll("[^a-zA-Z0-9]", " ");
			for(String feature : c.getFeatures()){
				if(feature.startsWith("Category") && 
					categoryClassifier.classify(sequence).bestCategory().equals(feature.substring("Category ".length()))){
					++categoryCount;
				} else if(feature.startsWith("AssignGroup")){
					feature = feature.substring(12);
					JointClassification rawResult = assignGroupClassifier.classify(sequence);
					for(int i = 0; i < num; ++i){
						if(feature.equals(rawResult.category(i))){
							++assignGroupCount;
							break;
						}
					}
				} else if(feature.startsWith("Coordinator")){
					feature = feature.substring(12);
					JointClassification rawResult = coorditorClassifier.classify(sequence);
					for(int i = 0; i < num; ++i){
						if(feature.equals(rawResult.category(i))){
							++coordinatorCount;
							break;
						}
					}
				} else if(feature.startsWith("Logical")){
					JointClassification rawResult = affectedCiClassifier.classify(sequence);
					feature = feature.substring(8);
					for(int i = 0; i < num; ++i){
						if(feature.equals(rawResult.category(i))){
							++affectedCiCount;
							break;
						}
					}
				} else if(feature.startsWith("Gl")){
					feature = feature.substring(3);
					JointClassification rawResult = modelClassifier.classify(sequence);
					for(int i = 0; i < num; ++i){
						if(feature.equals(rawResult.category(i))){
							++modelCount;
							break;
						}
					}
				}
			}
			if(count % 100 == 0){
				System.out.println("Assigned Group: " + (assignGroupCount / count));
				System.out.println("Coordinator: " + (coordinatorCount / count));
				System.out.println("Affected CI: " + (affectedCiCount / count));
				System.out.println("Model: " + (modelCount / count));
				System.out.println("Category: " + (categoryCount / count) + "\r\n");
			}
			++count;
		}
		System.out.println("Assigned Group: " + (assignGroupCount / testSet.size()));
		System.out.println("Coordinator: " + (coordinatorCount / testSet.size()));
		System.out.println("Affected CI: " + (affectedCiCount / testSet.size()));
		System.out.println("Model: " + (modelCount / testSet.size()));
		System.out.println("Category: " + (categoryCount / testSet.size()) + "\r\n");
	}
}

